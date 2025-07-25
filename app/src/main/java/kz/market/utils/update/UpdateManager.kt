package kz.market.utils.update

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File

class UpdateManager(
    private val appContext: Context,
    private val repoOwner: String,
    private val repoName: String
) {
    private val client = OkHttpClient()
    private var progressJob: Job? = null

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) return@withContext null

        try {
            val request = Request.Builder()
                .header("Accept", "application/vnd.github.v3+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .url("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body.string()

            val json = JSONObject(body)
            val remoteVersion = json.getString("tag_name").removePrefix("v")
            val assets = json.getJSONArray("assets")
            val asset = (0 until assets.length())
                .asSequence()
                .map { assets.getJSONObject(it) }
                .firstOrNull { it.getString("name").endsWith(".apk") }
                ?: return@withContext null

            val currentVersion = appContext.packageManager
                .getPackageInfo(appContext.packageName, 0).versionName ?: "0.0.0"

            return@withContext when {
                remoteVersion != currentVersion -> UpdateInfo(
                    version = remoteVersion,
                    downloadUrl = asset.getString("browser_download_url"),
                    changelog = json.optString("body")
                )

                else -> null
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "checkForUpdates failed", e)
            null
        }
    }

    fun startDownload(info: UpdateInfo): Long {
        val apkFile = File(
            appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )

        if (apkFile.exists()) apkFile.delete()

        val dm = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(info.downloadUrl.toUri())
            .setTitle("Скачивание обновления")
            .setMimeType("application/vnd.android.package-archive")
            .setDestinationInExternalFilesDir(
                appContext,
                Environment.DIRECTORY_DOWNLOADS,
                "update.apk"
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val id = dm.enqueue(request)
        UpdatePrefs.saveId(appContext, id)
        return id
    }


    fun observeDownloadProgress(
        downloadId: Long,
        scope: CoroutineScope,
        onProgress: (Int) -> Unit,
        onCancelled: () -> Unit,
        onSuccess: () -> Unit,
        onNetworkError: () -> Unit,
        onFailure: () -> Unit
    ) {
        val dm = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        progressJob?.cancel()
        progressJob = scope.launch(Dispatchers.IO) {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = dm.query(query)

                if (cursor == null || !cursor.moveToFirst()) onCancelled()

                if (cursor != null && cursor.moveToFirst()) {
                    val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val reason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)

                    if (downloadedIndex != -1 && totalIndex != -1 && statusIndex != -1) {
                        val downloaded = cursor.getLong(downloadedIndex)
                        val total = cursor.getLong(totalIndex)
                        val status = cursor.getInt(statusIndex)

                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                withContext(Dispatchers.Main) {
                                    onProgress(100)
                                    onSuccess()
                                }
                                downloading = false
                            }

                            DownloadManager.STATUS_FAILED -> {
                                withContext(Dispatchers.Main) {
                                    onFailure()
                                }
                                downloading = false
                            }

                            DownloadManager.STATUS_PAUSED -> {
                                val reasonCode = cursor.getInt(reason)

                                if (reasonCode == DownloadManager.PAUSED_WAITING_TO_RETRY && !isNetworkAvailable()) {
                                    withContext(Dispatchers.Main) {
                                        onNetworkError()
                                    }
                                }
                            }

                            DownloadManager.STATUS_RUNNING -> {
                                if (total > 0) {
                                    val percent = (downloaded * 100 / total).toInt().coerceIn(0, 100)
                                    withContext(Dispatchers.Main) {
                                        onProgress(percent)
                                    }
                                }
                            }
                        }
                    }
                }
                cursor?.close()
                delay(300L)
            }
        }
    }


    fun cancelProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = appContext.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

}

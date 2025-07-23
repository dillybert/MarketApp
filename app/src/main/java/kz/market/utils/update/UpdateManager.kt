// kz/market/update/UpdateManager.kt
package kz.market.utils.update

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class UpdateManager(
    private val appContext: Context,
    private val repoOwner: String,
    private val repoName: String
) {
    private val client = OkHttpClient()

    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable()) return@withContext null

        try {
            val request = Request.Builder()
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

            return@withContext if (isNewer(remoteVersion, currentVersion)) {
                UpdateInfo(
                    version = remoteVersion,
                    downloadUrl = asset.getString("browser_download_url"),
                    changelog = json.optString("body")
                )
            } else null
        } catch (e: Exception) {
            Log.e("UpdateManager", "checkForUpdates failed", e)
            null
        }
    }

    fun startDownload(info: UpdateInfo) {
        val dm = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(info.downloadUrl.toUri())
            .setTitle("Скачивание обновления")
            .setDestinationInExternalFilesDir(
                appContext,
                Environment.DIRECTORY_DOWNLOADS,
                "update.apk"
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        val id = dm.enqueue(request)
        UpdatePrefs.saveId(appContext, id)
    }


    private fun isNewer(remote: String, local: String): Boolean {
        val r = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val l = local.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0..2) {
            val rv = r.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (rv != lv) return rv > lv
        }
        return false
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = appContext.getSystemService(ConnectivityManager::class.java)
        val n = cm.activeNetworkInfo
        return n?.isConnected == true
    }
}

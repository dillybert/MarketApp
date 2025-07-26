package kz.market.service.system

import android.content.Context
import android.os.Environment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedOutputStream

class DownloadWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(
    appContext = ctx,
    params = params
) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val apkUrl = inputData.getString("apk_url") ?: return@withContext Result.failure()
        val file = File(
            applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "update.apk"
        )

        if (file.exists()) file.delete()

        try {
            val request = Request.Builder()
                .header("Accept", "application/octet-stream")
                .header("User-Agent", "AndroidApp/1.0")
                .url(apkUrl)
                .build()

            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure()

                val body = response.body
                val totalBytes = body.contentLength()

                body.byteStream()
                    .buffered(64 * 1024)              // буфер 64 КБ на чтение
                    .use { input ->
                        BufferedOutputStream(
                            FileOutputStream(file),
                            64 * 1024                  // буфер 64 КБ на запись
                        ).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var downloadedBytes = 0L
                            var read: Int

                            var lastProgress = -1
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                                val progress = (downloadedBytes * 100 / totalBytes).toInt().coerceIn(0, 100)
                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    setProgress(workDataOf(
                                            "progress" to progress,
                                            "downloadedBytes" to downloadedBytes,
                                            "totalBytes" to totalBytes
                                        )
                                    )
                                }
                            }
                        }
                    }

            }

            return@withContext Result.success(
                workDataOf(
                    "apk_file_path" to file.absolutePath
                )
            )
        } catch (e: IOException) {
            if (e.message?.contains("ENOSPC") == true) {
                return@withContext Result.failure(
                    workDataOf("error" to "No space left on device")
                )
            }

            e.printStackTrace()
            return@withContext Result.retry()
        }
    }
}
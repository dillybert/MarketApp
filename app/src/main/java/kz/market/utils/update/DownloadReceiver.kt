package kz.market.utils.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.edit
import androidx.core.net.toUri

object UpdatePrefs {
    private const val NAME = "update_prefs"
    private const val KEY_ID = "download_id"

    fun saveId(ctx: Context, id: Long) =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit { putLong(KEY_ID, id) }

    fun loadId(ctx: Context): Long =
        ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .getLong(KEY_ID, -1L)
}


class DownloadCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return

        Log.d("DownloadCompleteReceiver", "onReceive: ${intent.action}")

        val finishedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        val expectedId = UpdatePrefs.loadId(context)
        if (finishedId != expectedId) return

        val dm = context.getSystemService(DownloadManager::class.java)
        val uri = dm.getUriForDownloadedFile(finishedId)
            ?: return

        installApk(context, uri)
    }

    private fun installApk(ctx: Context, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pm = ctx.packageManager
            if (!pm.canRequestPackageInstalls()) {
                val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = "package:${ctx.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                ctx.startActivity(i)
                return
            }
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        ctx.startActivity(intent)
    }
}


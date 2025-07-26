package kz.market.service.system

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kz.market.service.utils.UpdateStatus
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class Installer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _installStatus = MutableSharedFlow<UpdateStatus>(replay = 1)
    val installStatus: MutableSharedFlow<UpdateStatus> = _installStatus

    val actionInstallResult: String = "${context.packageName}.UPDATE_INSTALL_RESULT"
    private var pendingApkFile: File? = null

    fun install(apkFile: File) {
        try {
            pendingApkFile = apkFile
            _installStatus.tryEmit(UpdateStatus.Installing)

            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            )

            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            FileInputStream(apkFile).use { inputStream ->
                session.openWrite("update.apk", 0, apkFile.length()).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    session.fsync(outputStream)
                }
            }

            val intent = Intent(actionInstallResult).apply {
                `package` = context.packageName
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                else
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            ContextCompat.registerReceiver(
                context,
                installResultReceiver,
                IntentFilter(actionInstallResult),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            session.commit(pendingIntent.intentSender)
            session.close()
        } catch (e: Exception) {
            pendingApkFile?.delete()
            pendingApkFile = null
            _installStatus.tryEmit(UpdateStatus.Error(e.message))
        }
    }

    private val installResultReceiver = object : BroadcastReceiver() {
        @SuppressLint("UnsafeIntentLaunch")
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
            val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                    if (confirmIntent != null) {
                        _installStatus.tryEmit(UpdateStatus.InstallPending)
                        confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(confirmIntent)
                        return
                    } else {
                        _installStatus.tryEmit(UpdateStatus.Error("User action required but intent is null"))
                    }
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    _installStatus.tryEmit(UpdateStatus.InstallSuccess)
                    cleanUp()
                }

                else -> {
                    _installStatus.tryEmit(UpdateStatus.Error(message))
                    cleanUp()
                }
            }
        }

        private fun cleanUp() {
            pendingApkFile?.let {
                if (it.delete().not()) {
                    Log.d("Installer", "Failed to delete update file")
                }
                pendingApkFile = null
            }
            context.unregisterReceiver(this)
        }
    }
}
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
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateStatus
import kz.market.utils.SharedPrefs
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

    fun install(apkFile: File, digest: String?) {
        try {
            if (digest != null) {
                val expectedSHA256 = digest.removePrefix("sha256:").lowercase()
                val calculatedSHA256 = calculateSHA256(apkFile).lowercase()

                if (expectedSHA256 != calculatedSHA256) {
                    _installStatus.tryEmit(
                        UpdateStatus.Error(
                            "Invalid SHA-256 digest. APK may be tampered"
                        )
                    )
                    return
                }
            }

            pendingApkFile = apkFile
            _installStatus.tryEmit(UpdateStatus.Installing)

            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL
            )

            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)

            FileInputStream(apkFile).use { inputStream ->
                session.openWrite(
                    "update.apk",
                    0,
                    apkFile.length()
                ).use { outputStream ->
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

            session.commit(pendingIntent.intentSender)
            session.close()
        } catch (e: Exception) {
            pendingApkFile?.delete()
            pendingApkFile = null
            _installStatus.tryEmit(UpdateStatus.Error(e.message))
        }
    }

    private fun calculateSHA256(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var read: Int
            while (fis.read(buffer).also { read = it } > 0) {
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
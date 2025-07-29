package kz.market.data.installer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kz.market.domain.repository.ApkInstaller
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class ApkInstallerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ApkInstaller {
    val actionInstallResult: String = "${context.packageName}.UPDATE_INSTALL_RESULT"
    private var pendingApkFile: File? = null

    override fun installApk(apkFile: File, digest: String): Result<Unit> {
        return try {
            val expectedSHA256 = digest.removePrefix("sha256:").lowercase()
            val calculatedSHA256 = calculateSHA256(apkFile).lowercase()

            if (expectedSHA256 != calculatedSHA256) {
                return Result.failure(
                    RuntimeException("Invalid SHA-256 digest. APK may be tampered")
                )
            }

            pendingApkFile = apkFile

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

            Result.success(Unit)
        } catch (e: Exception) {
            pendingApkFile?.delete()
            pendingApkFile = null
            Result.failure(
                RuntimeException(e.message ?: "Unknown error")
            )
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
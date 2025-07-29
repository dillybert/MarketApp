package kz.market.domain.repository

import java.io.File
import kotlin.jvm.Throws

interface ApkInstaller {
    fun installApk(apkFile: File, digest: String): Result<Unit>
}
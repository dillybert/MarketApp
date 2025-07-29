package kz.market.domain

import kz.market.domain.model.UpdateMetaData
import java.io.File

sealed class UpdateStatus {
    object Idle : UpdateStatus()

    data class Available(
        val metaData: UpdateMetaData
    ) : UpdateStatus()

    data class Downloading(
        val progress: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateStatus()

    data class Downloaded(
        val apkFile: File,
        val digest: String
    ) : UpdateStatus()

    object Installing : UpdateStatus()

    object InstallPending : UpdateStatus()

    object Installed : UpdateStatus()

    data class Error(
        val message: String
    ) : UpdateStatus()
}
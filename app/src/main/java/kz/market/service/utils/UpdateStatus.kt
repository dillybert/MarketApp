package kz.market.service.utils

import kz.market.service.model.UpdateMetaData
import java.io.File

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    data class UpdateAvailable(val updateMetaData: UpdateMetaData) : UpdateStatus()
    data class Downloading(val totalBytes: Long, val progress: Int, val downloadedBytes: Long) : UpdateStatus()
    data class DownloadComplete(val apkFile: File) : UpdateStatus()
    object Installing : UpdateStatus()
    object InstallPending : UpdateStatus()
    object InstallSuccess : UpdateStatus()
    data class Error(val message: String?) : UpdateStatus()
}

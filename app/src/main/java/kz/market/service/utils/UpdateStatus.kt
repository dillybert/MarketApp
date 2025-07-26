package kz.market.service.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kz.market.R
import kz.market.service.model.UpdateMetaData
import java.io.File

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    data class UpdateAvailable(val updateMetaData: UpdateMetaData) : UpdateStatus()
    data class Downloading(val contentSize: Long, val progress: Int, val downloaded: Long) : UpdateStatus()
    data class DownloadComplete(val apkFile: File) : UpdateStatus()
    object Installing : UpdateStatus()
    object InstallPending : UpdateStatus()
    object InstallSuccess : UpdateStatus()
    data class Error(val message: String?) : UpdateStatus()
}

fun UpdateStatus.toDialogData(onConfirm: () -> Unit, onDismiss: () -> Unit): UpdateDialogData? = when (this) {
    is UpdateStatus.UpdateAvailable -> UpdateDialogData(
        icon = {
            Icon(
                modifier = Modifier.size(56.dp),
                painter = painterResource(id = R.drawable.ic_package),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = "Доступно обновление",
        message = "Доступно новая версия ${updateMetaData.remoteVersionTag}.",
        confirmButtonText = "Обновить",
        dismissButtonText = "Позже",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )

    is UpdateStatus.Downloading -> UpdateDialogData(
        icon = {
            Icon(
                modifier = Modifier.size(56.dp),
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = "Загрузка обновления",
        message = "Пожалуйста, подождите пока обновление загружается",
        isProgress = true,
        contentSize = contentSize,
        progress = progress,
        downloaded = downloaded
    )

    is UpdateStatus.Error -> UpdateDialogData(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_x_circle),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = "Ошибка при обновлений",
        message = message ?: "Неизвестная ошибка при обновлении"
    )

    is UpdateStatus.InstallPending -> UpdateDialogData(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_package_add),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = "Подтвердите обновление",
        message = "Пожалуйста, подтвердите обновление"
    )

    is UpdateStatus.InstallSuccess -> UpdateDialogData(
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = "Обновление установлено",
        message = "Обновление установлено успешно"
    )

    is UpdateStatus.Installing -> UpdateDialogData(
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = "Установка обновления",
        message = "Пожалуйста, подождите пока обновление будет установлено"
    )

    else -> null
}

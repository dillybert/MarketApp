package kz.market.service.utils

import androidx.compose.runtime.Composable

data class UpdateDialogData(
    val icon: @Composable (() -> Unit)? = null,
    val title: String,
    val message: String,
    val confirmButtonText: String? = null,
    val dismissButtonText: String? = null,
    val onConfirm: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val contentSize: Long = 0L,
    val isProgress: Boolean = false,
    val progress: Int = 0,
    val downloaded: Long = 0L
)
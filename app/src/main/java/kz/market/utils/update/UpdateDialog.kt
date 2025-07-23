package kz.market.utils.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UpdateDialog(
    info: UpdateInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Доступно обновление") },
        text = {
            Column {
                Text("Новая версия ${info.version}. Обновить сейчас?")
                if (!info.changelog.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(info.changelog)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Обновить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Позже") }
        }
    )
}

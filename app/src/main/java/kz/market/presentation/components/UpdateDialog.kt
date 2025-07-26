package kz.market.presentation.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kz.market.R
import kz.market.service.utils.UpdateStatus
import java.io.File
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun LoadingProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    foregroundColor: Color = ProgressIndicatorDefaults.linearColor,
    animationSpec: AnimationSpec<Float> = ProgressIndicatorDefaults.ProgressAnimationSpec
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius))
                .background(foregroundColor)
        )
    }
}

fun Long.formatFileSize(): String {
    if (this <= 0L) return "0 B"

    val units = listOf("B", "KB", "MB", "GB")
    val digitGroup = (log10(this.toDouble()) / log10(1024.0)).toInt()
    val size = this / 1024.0.pow(digitGroup)

    return "%.2f %s".format( size, units.getOrElse(digitGroup) { "???" })
}


@Composable
fun UpdateDialog(
    updateStatus: UpdateStatus,
    onUpdateInstall: (apkFile: File) -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    onDismiss: () -> Unit,
    shape: RoundedCornerShape = RoundedCornerShape(10.dp)
) {
    LaunchedEffect(updateStatus) {
        if (updateStatus is UpdateStatus.DownloadComplete) {
            onUpdateInstall(updateStatus.apkFile)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    when (val data = updateStatus) {
        is UpdateStatus.Downloading -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        modifier = Modifier.size(56.dp),
                        painter = painterResource(id = R.drawable.ic_download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Загрузка обновления",
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            text = "Пожалуйста, подождите пока обновление загружается",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${data.downloadedBytes.formatFileSize()} из ${data.totalBytes.formatFileSize()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${data.progress}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Icon(
                                        painter = painterResource(R.drawable.ic_loader),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .graphicsLayer {
                                                rotationZ = rotation
                                            },
                                    )
                                }
                            }
                            LoadingProgressIndicator(
                                progress = data.progress / 100f,
                                cornerRadius = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {},
                shape = shape
            )
        }

        is UpdateStatus.Error -> {
            if (data.message?.contains("aborted", ignoreCase = true) == true ||
                data.message?.contains("cancelled", ignoreCase = true) == true) {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = "Обновление отменено",
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                text = "Вы отменили установку обновления. Приложение продолжит работу.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = dismissButton,
                    shape = shape
                )
            } else {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_x_circle),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = "Ошибка при обновлений",
                            textAlign = TextAlign.Center,
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Text(
                                text = data.message ?: "Неизвестная ошибка при обновлении",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = dismissButton,
                    shape = shape
                )
            }
        }

        is UpdateStatus.InstallPending -> {
            AlertDialog(
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                ),
                onDismissRequest = {},
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_loader),
                        contentDescription = null,
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer {
                                rotationZ = rotation
                            },
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                },
                title = {
                    Text(
                        text = "Устанавливаем обновление",
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            text = "Пожалуйста, подождите пока обновление устанавливается",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {},
                dismissButton = {},
                shape = shape
            )
        }

        is UpdateStatus.UpdateAvailable -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                icon = {
                    Icon(
                        modifier = Modifier.size(56.dp),
                        painter = painterResource(id = R.drawable.ic_package),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = {
                    Text(
                        text = "Доступно обновление",
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Установлено:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "v${data.updateMetaData.currentVersionTag}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 3.dp, horizontal = 6.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Доступно:",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "v${data.updateMetaData.remoteVersionTag}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(vertical = 3.dp, horizontal = 6.dp)
                            )
                        }

                        if (data.updateMetaData.description.isNotEmpty()) {
                            Text(
                                text = "Что нового:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = data.updateMetaData.description,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Text(
                            text = "Хотите установить сейчас?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = confirmButton,
                dismissButton = dismissButton,
                shape = shape
            )
        }

        else -> Unit
    }
}
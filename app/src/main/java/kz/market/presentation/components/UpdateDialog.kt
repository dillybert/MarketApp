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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kz.market.R
import kz.market.service.utils.UpdateStatus
import kz.market.service.utils.toDialogData
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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    LaunchedEffect(updateStatus) {
        if (updateStatus is UpdateStatus.DownloadComplete) {
            onUpdateInstall(updateStatus.apkFile)
        }
    }

    val dialogData = updateStatus.toDialogData(onConfirm = onConfirm, onDismiss = onDismiss)
    dialogData ?: return

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

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = dialogData.icon,
        title = {
            Text(
                text = dialogData.title,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = dialogData.message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (dialogData.isProgress) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${dialogData.downloaded.formatFileSize()} из ${dialogData.contentSize.formatFileSize()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${dialogData.progress}%",
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
                            progress = dialogData.progress / 100f,
                            cornerRadius = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            dialogData.confirmButtonText?.let {
                Button(
                    onClick = dialogData.onConfirm!!,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = it
                    )
                }
            }
        },

        dismissButton = {
            dialogData.dismissButtonText?.let {
                TextButton(
                    onClick = dialogData.onDismiss!!,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = it
                    )
                }
            }
        },
        shape = RoundedCornerShape(10.dp)
    )
}
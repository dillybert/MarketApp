package kz.market.presentation.components.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.*

@Composable
fun CameraViewfinderOverlay(
    modifier: Modifier = Modifier,
    frameWidth: Dp = 250.dp,
    frameHeight: Dp = 130.dp,
    borderColor: Color = Color.Green,
    borderWidth: Dp = 2.dp,
    cornerLength: Dp = 20.dp,
    cornerStroke: Dp = 4.dp,
    cornerRadius: Dp = 8.dp,
    laserEnabled: Boolean = false
) {
    // ⏳ Анимации появления
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
    }

    // 🔁 Лазер: бесконечная только после scale & alpha
    val showLaser = scale.value >= 0.99f
    val laserY = rememberInfiniteTransition(label = "LaserAnim").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val currentScale = scale.value

        val framePxWidth = frameWidth.toPx() * currentScale
        val framePxHeight = frameHeight.toPx() * currentScale
        val frameLeft = (canvasWidth - framePxWidth) / 2f
        val frameTop = (canvasHeight - framePxHeight) / 2f

        val frameRect = Rect(
            frameLeft,
            frameTop,
            frameLeft + framePxWidth,
            frameTop + framePxHeight
        )

        // 1. Затемняем всё — кроме рамки
        val outerPath = Path().apply { addRect(Rect(Offset.Zero, size)) }
        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = frameRect,
                    cornerRadius = CornerRadius(cornerRadius.toPx() * currentScale)
                )
            )
        }
        drawPath(
            path = Path.combine(PathOperation.Difference, outerPath, cutoutPath),
            color = Color.Black.copy(alpha = 0.6f)
        )

        // 2. Акцентные углы
        val corner = cornerLength.toPx() * currentScale
        val stroke = cornerStroke.toPx()
        val radius = cornerRadius.toPx() * currentScale
        val paint = Stroke(width = stroke, cap = StrokeCap.Round)

        fun drawCornerArcAndLines(topLeft: Offset, angleStart: Float) {
            drawArc(
                color = borderColor,
                startAngle = angleStart,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(radius * 2, radius * 2),
                style = paint
            )
        }

        fun drawCornerLines(p1: Offset, p2: Offset) {
            drawLine(
                color = borderColor,
                start = p1,
                end = p2,
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        // Top-left
        drawCornerArcAndLines(Offset(frameRect.left, frameRect.top), 180f)
        drawCornerLines(
            Offset(frameRect.left + radius, frameRect.top),
            Offset(frameRect.left + corner, frameRect.top)
        )
        drawCornerLines(
            Offset(frameRect.left, frameRect.top + radius),
            Offset(frameRect.left, frameRect.top + corner)
        )

        // Top-right
        drawCornerArcAndLines(Offset(frameRect.right - radius * 2, frameRect.top), 270f)
        drawCornerLines(
            Offset(frameRect.right - radius, frameRect.top),
            Offset(frameRect.right - corner, frameRect.top)
        )
        drawCornerLines(
            Offset(frameRect.right, frameRect.top + radius),
            Offset(frameRect.right, frameRect.top + corner)
        )

        // Bottom-right
        drawCornerArcAndLines(Offset(frameRect.right - radius * 2, frameRect.bottom - radius * 2), 0f)
        drawCornerLines(
            Offset(frameRect.right - radius, frameRect.bottom),
            Offset(frameRect.right - corner, frameRect.bottom)
        )
        drawCornerLines(
            Offset(frameRect.right, frameRect.bottom - radius),
            Offset(frameRect.right, frameRect.bottom - corner)
        )

        // Bottom-left
        drawCornerArcAndLines(Offset(frameRect.left, frameRect.bottom - radius * 2), 90f)
        drawCornerLines(
            Offset(frameRect.left + radius, frameRect.bottom),
            Offset(frameRect.left + corner, frameRect.bottom)
        )
        drawCornerLines(
            Offset(frameRect.left, frameRect.bottom - radius),
            Offset(frameRect.left, frameRect.bottom - corner)
        )

        // 3. Лазер — только когда всё отрисовалось
        if (laserEnabled && showLaser) {
            val laserYPos = frameRect.top + laserY.value * frameRect.height
            drawLine(
                color = Color.Red.copy(alpha = 0.8f),
                start = Offset(frameRect.left + 6.dp.toPx(), laserYPos),
                end = Offset(frameRect.right - 6.dp.toPx(), laserYPos),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = Color.Red.copy(alpha = 0.12f),
                start = Offset(frameRect.left + 6.dp.toPx(), laserYPos - 3.dp.toPx()),
                end = Offset(frameRect.right - 6.dp.toPx(), laserYPos - 3.dp.toPx()),
                strokeWidth = 6.dp.toPx()
            )
        }
    }
}
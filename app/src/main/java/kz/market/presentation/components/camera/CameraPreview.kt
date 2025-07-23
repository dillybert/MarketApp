package kz.market.presentation.components.camera

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier,
    onBarcodeScanned: (String) -> Unit
) {
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()

        onDispose {
            cameraProvider.unbindAll()
            cameraExecutor.shutdown() // avoid leaking threads
        }
    }

    LaunchedEffect(previewView) {
        val rotation = previewView.display.rotation
        val cameraProvider = withContext(Dispatchers.IO) {
            cameraProviderFuture.get()
        }

        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setTargetRotation(rotation)
            .build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        val analysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .apply {
                // Force FPS
                Camera2Interop.Extender(this)
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 30))
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(onBarcodeScanned))
            }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                analysis
            )
        } catch (e: Exception) {
            Log.e("CameraPreview", "Camera initialization failed", e)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView },
    )
}
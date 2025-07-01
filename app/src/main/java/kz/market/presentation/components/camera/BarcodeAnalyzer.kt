package kz.market.presentation.components.camera

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E
            )
            .build()
    )

    // Optionally, debounce multiple scans
    private val isLocked = AtomicBoolean(false)
    private var lastScanned: String? = null
    private var lastScanTime = 0L
    private val scanCooldownMillis = 2000L // 2 sec debounce window

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        if (!isLocked.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val now = System.currentTimeMillis()
                barcodes.firstOrNull { it.rawValue != null }?.rawValue?.let { value ->
                    if (value != lastScanned || now - lastScanTime > scanCooldownMillis) {
                        isLocked.set(true)
                        lastScanned = value
                        lastScanTime = now

                        onBarcodeDetected(value)

                        Handler(Looper.getMainLooper()).postDelayed({
                            isLocked.set(false)
                        }, scanCooldownMillis)

                        Log.d("BarcodeAnalyzer", "Detected barcode: $value")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeAnalyzer", "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                isLocked.set(false)
                imageProxy.close()
            }
    }
}
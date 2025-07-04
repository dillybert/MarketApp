package kz.market.presentation.components.camera

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onScan: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val bottomSheetState = rememberModalBottomSheetState()

    var requestingPermission by remember { mutableStateOf(false) }

    if (requestingPermission) {
        CameraPermissionHandler(
            onPermissionGranted = {
                requestingPermission = false
            },
            onPermissionDeniedPermanently = {
                requestingPermission = false
                Toast.makeText(context, "Пожалуйста, предоставьте разрешение на камеру в настройках", Toast.LENGTH_SHORT).show()

                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )

                onDismiss()
            },
            onPermissionDeniedTemporarily = {
                requestingPermission = false
                Toast.makeText(context, "Пожалуйста, предоставьте разрешение на камеру", Toast.LENGTH_SHORT).show()

                onDismiss()
            }
        )
    }

    LaunchedEffect(visible) {
        if (visible) {
            requestingPermission = true
        }
    }

    if (visible && !requestingPermission) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            contentColor = Color.Transparent,
            containerColor = Color.Transparent,
            dragHandle = {}
        ) {
            Box(
                modifier = Modifier
                    .height(400.dp)
            ) {
                CameraPreview(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds(),
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onBarcodeScanned = { barcode ->
                        onScan(barcode)
                        onDismiss()
                    }
                )

                CameraViewfinderOverlay(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}
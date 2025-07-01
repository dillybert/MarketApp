package kz.market.presentation.screens.reports

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kz.market.R
import kz.market.presentation.components.CameraViewfinderOverlay
import kz.market.presentation.components.camera.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onDetailsClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permission = Manifest.permission.CAMERA
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(permission)
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Reports")
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_package_add),
                            contentDescription = "Package"
                        )
                    }

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_package_add),
                            contentDescription = "Package"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
//        when(getProductState) {
//            is UIGetState.Loading -> {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            is UIGetState.Success -> {
//                val products = (getProductState as UIGetState.Success<List<Product>>).data
//
//                LazyColumn{
//                    items(products) { product ->
//                        Text(
//                            text = "${product.barcode} ${product.name} - ${product.price}",
//                            modifier = Modifier
//                                .clickable { onDetailsClick() }
//                        )
//                    }
//                }
//            }
//
//            is UIGetState.Error -> {
//
//            }
//
//            is UIGetState.Empty -> {
//                Column (
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    val text = remember { mutableStateOf("") }
//
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_package),
//                        contentDescription = "Storage is empty",
//                        modifier = Modifier.size(100.dp),
//                        colorFilter = ColorFilter.tint(Color.Gray.copy(alpha = 0.5f))
//                    )
//
//                    Text(
//                        text = "No products found",
//                        style = MaterialTheme.typography.bodyLarge,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.Gray.copy(alpha = 0.5f)
//                    )
//                }
//            }
//        }
//
//        Button(
//            modifier = Modifier
//                .padding(12.dp)
//                .fillMaxWidth(),
//            onClick = {
//                val product = Product(name = "Product 1", price = 10.0)
//                viewModel.addProduct(product)
//            },
//            enabled = addProductState !is UISetState.Loading
//        ) {
//            when(addProductState) {
//                is UISetState.Loading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .size(20.dp)
//                            .padding(2.dp),
//                        strokeWidth = 2.dp,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                else -> {
//                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
//                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
//                    Text("Add Product")
//                }
//            }
//        }


            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = bottomSheetState,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
                            onBarcodeScanned = {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                onDetailsClick()
                            }
                        )

                        CameraViewfinderOverlay(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun ReportsScreenPreview() {
    ReportsScreen(onDetailsClick = {})
}
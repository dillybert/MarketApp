package kz.market.presentation.screens.storage

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kz.market.R
import kz.market.domain.models.Product
import kz.market.presentation.components.CameraViewfinderOverlay
import kz.market.presentation.components.ProductItem
import kz.market.presentation.components.camera.CameraPreview
import kz.market.utils.UIGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreenContent(
    state: UIGetState<List<Product>>,
    searchText: String,
    onDetailsClick: (barcode: String, name: String, price: Double, ownPrice: Double, quantity: Int, unit: String) -> Unit,
    onSearchTextChanged: (String) -> Unit,
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
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Склад")
                },
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = "Add New Products"
                        )
                    }

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_file_list),
                            contentDescription = "List of added products"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        when (state) {
            is UIGetState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.5f)
                            .aspectRatio(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxWidth(.5f)
                                .aspectRatio(1f),
                            painter = painterResource(R.drawable.ic_package_add),
                            tint = MaterialTheme.colorScheme.secondary,
                            contentDescription = "Storage is empty",
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Склад пустой",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            is UIGetState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxWidth(.3f)
                                .aspectRatio(1f),
                            painter = painterResource(R.drawable.ic_x_circle),
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = "Error"
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Ошибка при загрузке данных",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            is UIGetState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UIGetState.Success<List<Product>> -> {
                val productsList = state.data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            value = searchText,
                            onValueChange = { newText ->
                                onSearchTextChanged(newText)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_search),
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                                )
                            },
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = "Поиск по названию или штрих-коду товара",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        showBottomSheet = true
                                    },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_scan),
                                        contentDescription = "Search with scanner"
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    when {
                        productsList.isEmpty() -> {
                            item {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    text = "Не найдено",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        else -> {
                            items(productsList) { item ->
                                ProductItem(
                                    product = item,
                                    onClick = { product ->
                                        onDetailsClick(
                                            product.barcode,
                                            product.name,
                                            product.price,
                                            product.ownPrice,
                                            product.quantity,
                                            product.unit
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
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
                            onSearchTextChanged(barcode)
                            showBottomSheet = false
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

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    onDetailsClick: (barcode: String, name: String, price: Double, ownPrice: Double, quantity: Int, unit: String) -> Unit
) {
    StorageScreenContent(
        state = viewModel.productsState.collectAsState().value,
        searchText = viewModel.searchQueryForProducts.collectAsState().value,
        onDetailsClick = onDetailsClick,
        onSearchTextChanged = viewModel::updateSearchQuery
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun StorageScreenPreview() {
    StorageScreenContent(
        state = UIGetState.Success(data = listOf(
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
            Product(barcode = "5555555555555", name = "Test", price = 21.0, quantity = 10),
        )),
        searchText = "",
        onDetailsClick = { barcode, name, price, ownPrice, quantity, unit ->

        },
        onSearchTextChanged = {}
    )
}
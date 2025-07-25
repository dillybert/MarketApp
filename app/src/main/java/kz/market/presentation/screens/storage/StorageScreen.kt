package kz.market.presentation.screens.storage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kz.market.R
import kz.market.domain.models.Product
import kz.market.presentation.components.ProductItem
import kz.market.presentation.components.camera.CameraScannerSheet
import kz.market.presentation.utils.ProductFilter
import kz.market.utils.UIGetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreenContent(
    productsListState: UIGetState<List<Product>>,
    searchTextState: String,
    filterState: ProductFilter,
    onProductDetailsClick: (barcode: String) -> Unit,
    onProductAddButtonClick: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onCategoryFilterChange: (ProductFilter) -> Unit
) {
    var showCameraScanner by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Склад")
                },
                actions = {
                    IconButton(
                        onClick = onProductAddButtonClick,
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

        when (productsListState) {
            is UIGetState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.5f),
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

                        Text(
                            modifier = Modifier
                                .padding(vertical = 20.dp),
                            text = "На складе пока нет товаров",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Button(
                            onClick = onProductAddButtonClick
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = "Add New Products"
                            )

                            Text(
                                modifier = Modifier
                                    .padding(start = 8.dp),
                                text = "Добавить товар"
                            )
                        }
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
                            text = productsListState.message,
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
                val productsList = productsListState.data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        vertical = 10.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    item {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            shape = RoundedCornerShape(10.dp),
                            value = searchTextState,
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
                                    text = "Поиск",
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        showCameraScanner = true
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

                    stickyHeader {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            items(ProductFilter.getAll()) { filter ->
                                FilterChip(
                                    selected = (filter == filterState),
                                    onClick = {
                                        onCategoryFilterChange(filter)
                                    },
                                    label = {
                                        Text(text = filter.displayName)
                                    },
                                    leadingIcon = {
                                        AnimatedVisibility(
                                            visible = (filter == filterState),
                                            enter = fadeIn() + expandHorizontally(),
                                            exit = shrinkHorizontally() + fadeOut()
                                        ) {
                                            Icon(
                                                modifier = Modifier
                                                    .width(18.dp),
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    when {
                        productsList.isEmpty() -> {
                            item {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    text = "Не найдено",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        else -> {
                            items(productsList, key = { it.barcode }) { item ->
                                ProductItem(
                                    modifier = Modifier
                                        .animateItem()
                                        .padding(horizontal = 14.dp),
                                    product = item,
                                    onClick = { product ->
                                        onProductDetailsClick(
                                            product.barcode,
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        CameraScannerSheet(
            visible = showCameraScanner,
            onDismiss = {
                showCameraScanner = false
            },
            onScan = { barcode ->
                showCameraScanner = false
                onSearchTextChanged(barcode)
            }
        )
    }
}

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    onProductDetailsClick: (barcode: String) -> Unit,
    onProductAddButtonClick: () -> Unit
) {
    StorageScreenContent(
        productsListState = viewModel.filteredProductsState.collectAsState().value,
        searchTextState = viewModel.searchQueryForProducts.collectAsState().value,
        filterState = viewModel.searchFilterForProducts.collectAsState().value,
        onProductDetailsClick = onProductDetailsClick,
        onProductAddButtonClick = onProductAddButtonClick,
        onSearchTextChanged = viewModel::updateSearchQuery,
        onCategoryFilterChange = viewModel::onCategoryFilterChange
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
        productsListState = UIGetState.Success(listOf(
            Product(
                barcode = "5555555555555",
                name = "Test Product",
                price = 21000.0,
                quantity = 100.0,
                unit = "шт"
            )
        )),
        searchTextState = "",
        filterState = ProductFilter.All,
        onProductDetailsClick = { barcode ->

        },
        onProductAddButtonClick = {},
        onSearchTextChanged = {},
        onCategoryFilterChange = {}
    )
}
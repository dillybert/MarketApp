package kz.market.presentation.screens.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kz.market.R
import kz.market.domain.models.Product
import kz.market.presentation.components.AutoCompleteTextField
import kz.market.presentation.components.ProductItem
import kz.market.presentation.components.camera.CameraScannerSheet
import kz.market.presentation.utils.SuggestionOption
import kz.market.presentation.utils.UnitOption
import kz.market.utils.UIGetState
import kz.market.utils.UISetState


sealed class DeleteResult {
    data class Success(val message: String) : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddScreen(
    addProductState: UISetState,
    productState: UIGetState<Product>,
    onBarcodeScanned: (String) -> Unit,
    onAddNewProductClick: (Product) -> Unit,
    onDetailsClick: (String) -> Unit
) {
    val inputState = remember {
        mutableStateOf(
            Product(
                supplier = Product.DEFAULT_SUPPLIER
            )
        )
    }

    val priceInput = remember { mutableStateOf("") }
    val ownPriceInput = remember { mutableStateOf("") }
    val quantityInput = remember { mutableStateOf("") }

    val unitOptions: List<UnitOption> = listOf(
        UnitOption("Килограмм", "кг"),
        UnitOption("Штучно", "шт"),
        UnitOption("Литр", "л")
    )

    val isProductExists = productState is UIGetState.Success<Product>

    var showInputError by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var showDropDown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(top = 3.dp, end = 10.dp),
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Text(
                    text = """ Перед добавлением товара, пожалуйста, убедитесь в следующем:
    • Название товара не пустое
    • Штрих-код введён корректно
    • Цена больше 0 (в тенге)
    • Количество больше 0
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

            }
        }

        item {
            val selectedLabel = unitOptions.find { it.value == inputState.value.unit }?.label
                ?: "Выберите единицу измерения"

            OutlinedTextField(
                isError = showInputError,
                singleLine = true,
                supportingText = {
                    if (showInputError) {
                        Text(
                            text = "Это поле обязательно для заполнения",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                value = inputState.value.barcode,
                onValueChange = {
                    inputState.value = inputState.value.copy(barcode = it)
                    onBarcodeScanned(it)
                },
                label = { Text("Штрих-код товара") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            showCameraScanner = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_scan),
                            contentDescription = "Barcode"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(),
                value = inputState.value.supplier,
                enabled = false,
                label = { Text("Поставщик товара") },
                onValueChange = {}
            )

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                isError = showInputError,
                singleLine = true,
                supportingText = {
                    if (showInputError) {
                        Text(
                            text = "Это поле обязательно для заполнения",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                value = inputState.value.name,
                onValueChange = { inputState.value = inputState.value.copy(name = it) },
                label = { Text("Название товара") }
            )


            OutlinedTextField(
                isError = showInputError,
                singleLine = true,
                supportingText = {
                    if (showInputError) {
                        Text(
                            text = "Это поле обязательно для заполнения",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                value = priceInput.value,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                        priceInput.value = it
                    }
                },
                trailingIcon = {
                    Icon(
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp),
                        painter = painterResource(R.drawable.ic_tenge_sign),
                        contentDescription = "Product price"
                    )
                },
                label = { Text("Реализационная цена товара") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )


            OutlinedTextField(
                isError = showInputError,
                singleLine = true,
                supportingText = {
                    if (showInputError) {
                        Text(
                            text = "Это поле обязательно для заполнения",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                value = ownPriceInput.value,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                        ownPriceInput.value = it
                    }
                },
                trailingIcon = {
                    Icon(
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp),
                        painter = painterResource(R.drawable.ic_tenge_sign),
                        contentDescription = "Product price"
                    )
                },
                label = { Text("Себестоимость товара") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )



            ExposedDropdownMenuBox(
                expanded = showDropDown,
                onExpandedChange = { showDropDown = !showDropDown }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedLabel,
                    onValueChange = {},
                    label = {
                        Text("Единица измерения товара")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(showDropDown)
                    },
                    modifier = Modifier
                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                )

                ExposedDropdownMenu(
                    expanded = showDropDown,
                    onDismissRequest = { showDropDown = false }
                ) {
                    unitOptions.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                inputState.value = inputState.value.copy(unit = value)
                                showDropDown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            OutlinedTextField(
                isError = showInputError,
                singleLine = true,
                supportingText = {
                    if (showInputError) {
                        Text(
                            text = "Это поле обязательно для заполнения",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                value = quantityInput.value,
                onValueChange = {
                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                        quantityInput.value = it
                    }
                },
                trailingIcon = {
                    unitOptions.find {
                        it.value == inputState.value.unit
                    }?.let {
                        Text(
                            text = it.value,
                        )
                    }
                },
                label = { Text("Остаток товара на складе") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    if (inputState.value.hasAnyMissingFields() &&
                        priceInput.value.isEmpty() &&
                        ownPriceInput.value.isEmpty() &&
                        quantityInput.value.isEmpty()) {
                        showInputError = true
                        return@Button
                    }

                    showInputError = false

                    inputState.value = inputState.value.copy(
                        price = priceInput.value.toDoubleOrNull() ?: 0.0,
                        ownPrice = ownPriceInput.value.toDoubleOrNull() ?: 0.0,
                        quantity = quantityInput.value.toDoubleOrNull() ?: 0.0
                    )

                    onAddNewProductClick(inputState.value)

                    inputState.value = Product.EMPTY
                    priceInput.value = ""
                    ownPriceInput.value = ""
                    quantityInput.value = ""
                },
                enabled = !isProductExists
            ) {
                Text(
                    text = "Добавить товар"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    CameraScannerSheet(
        visible = showCameraScanner,
        onDismiss = { showCameraScanner = false },
        onScan = { barcode ->
            inputState.value = inputState.value.copy(barcode = barcode)
            onBarcodeScanned(barcode)
            showCameraScanner = false
        }
    )
}


@Composable
fun RegisterInventoryArrivalScreen(

) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(
            vertical = 10.dp,
            horizontal = 14.dp
        )
    ) { 
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(top = 3.dp, end = 10.dp),
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Text(
                    text = """Перед оформлением прихода убедитесь, что:
    • Выбран правильный поставщик
    • Указана дата прихода
    • Все позиции и их количество верны
    • Цены на товары актуальны
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

            }
        }

        item {
            AutoCompleteTextField(
                value = text,
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text("Поставщик товара") },
                onValueChange = {
                    text = it
                },
                onSuggestionSelected = {
                    text = TextFieldValue(
                        it,
                        selection = TextRange(it.length)
                    )
                },
                suggestions = listOf(
                    SuggestionOption("Apple"),
                    SuggestionOption("Banana"),
                    SuggestionOption("Pineapple"),
                    SuggestionOption("Kiwi"),
                    SuggestionOption("Strawberry")
                )
            )
        }

        item {
            AutoCompleteTextField(
                value = text,
                modifier = Modifier
                    .fillMaxWidth(),
                label = { Text("Поставщик товара") },
                onValueChange = {
                    text = it
                },
                onSuggestionSelected = {
                    text = TextFieldValue(
                        it,
                        selection = TextRange(it.length)
                    )
                },
                suggestions = listOf(
                    SuggestionOption("Apple"),
                    SuggestionOption("Banana"),
                    SuggestionOption("Pineapple"),
                    SuggestionOption("Kiwi"),
                    SuggestionOption("Strawberry")
                )
            )
        }
    }


//    var isCheckboxChecked by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Column {
//            OutlinedTextField(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                value = "",
//                label = {
//                    Text("Приходная цена товара")
//                },
//                onValueChange = {},
//                enabled = !isCheckboxChecked
//            )
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                Checkbox(
//                    checked = isCheckboxChecked,
//                    onCheckedChange = {
//                        isCheckboxChecked = it
//                    }
//                )
//
//                Text(
//                    text = "Консигнация",
//                    style = MaterialTheme.typography.bodyMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .clickable(
//                            indication = null,
//                            interactionSource = remember { MutableInteractionSource() },
//                        ) {
//                            isCheckboxChecked = !isCheckboxChecked
//                        }
//                )
//            }
//        }
//    }



//    val context = LocalContext.current
//
//    // State to hold the selected date
//    val calendar = Calendar.getInstance()
//    val year = calendar.get(Calendar.YEAR)
//    val month = calendar.get(Calendar.MONTH)
//    val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//    var selectedDate by remember { mutableStateOf("$day/${month + 1}/$year") }
//
//    val datePickerDialog = DatePickerDialog(
//        context,
//        { _: DatePicker, pickedYear: Int, pickedMonth: Int, pickedDay: Int ->
//            selectedDate = "$pickedDay/${pickedMonth + 1}/$pickedYear"
//        },
//        year,
//        month,
//        day
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column {
//            Text(
//                text = "Выбранная дата: $selectedDate"
//            )
//
//            Spacer(modifier = Modifier.height(10.dp))
//
//            Button(
//                onClick = {
//                    datePickerDialog.show()
//                }
//            ) {
//                Text(text = "Open Date Picker")
//            }
//        }
//    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAddProductContent(
    addProductState: UISetState,
    deleteProductResult: UISetState,
    productState: UIGetState<Product>,
    onBarcodeScanned: (String) -> Unit,
    onAddNewProductClick: (Product) -> Unit,
    onDetailsClick: (String) -> Unit,
    onProductDeleteClick: (String) -> Unit,
    resetStates: () -> Unit
) {
    val tabNames = listOf(
        "Быстрое добавление",
        "Оформить приход"
    )

    var product = Product.EMPTY

    val bottomSheetState = rememberModalBottomSheetState()
    var bottomSheetVisible by remember { mutableStateOf(false) }
    var deleteResult: DeleteResult? by remember { mutableStateOf(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()
    val horizontalPagerState = rememberPagerState(
        initialPage = 1,
        initialPageOffsetFraction = 0f,
        pageCount = { tabNames.size }
    )


    LaunchedEffect(productState) {
        when (productState) {
            is UIGetState.Success<Product> -> {
                bottomSheetVisible = true
                product = Product(
                    barcode = productState.data.barcode,
                    name = productState.data.name,
                    price = productState.data.price,
                    ownPrice = productState.data.ownPrice,
                    quantity = productState.data.quantity,
                    unit = productState.data.unit,
                    createdAt = productState.data.createdAt,
                    updatedAt = productState.data.updatedAt
                )
            }

            else -> Unit
        }
    }


    LaunchedEffect(addProductState) {
        when (addProductState) {
            is UISetState.Error -> {
                snackBarHostState.showSnackbar(
                    message = addProductState.message,
                    withDismissAction = true
                )
                resetStates()
            }

            is UISetState.Success -> {
                snackBarHostState.showSnackbar(
                    message = "Товар успешно добавлен",
                    withDismissAction = true
                )
                resetStates()
            }

            else -> Unit
        }
    }

    LaunchedEffect(deleteProductResult) {
        when (deleteProductResult) {
            is UISetState.Error -> {
                deleteResult = DeleteResult.Error(deleteProductResult.message)
                resetStates()
            }

            is UISetState.Success -> {
                deleteResult = DeleteResult.Success("Товар успешно удалён")
                resetStates()
            }

            else -> Unit
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Добавление товара")
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow (
                selectedTabIndex = horizontalPagerState.currentPage
            ) {
                tabNames.forEachIndexed { index, label ->
                    Tab(
                        selected = horizontalPagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                horizontalPagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = label) },
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { index ->
                when (index) {
                    0 -> QuickAddScreen(
                        addProductState = addProductState,
                        productState = productState,
                        onBarcodeScanned = onBarcodeScanned,
                        onAddNewProductClick = onAddNewProductClick,
                        onDetailsClick = onDetailsClick
                    )

                    1 -> RegisterInventoryArrivalScreen()
                }
            }
        }


        if (bottomSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = {
                    bottomSheetVisible = false
                    deleteResult = null
                },
                sheetState = bottomSheetState,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            ) {
                when (deleteResult) {
                    is DeleteResult.Error -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = "Ошибка при удалений товара",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 20.dp
                                ),
                            text = (deleteResult as DeleteResult.Error).message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is DeleteResult.Success -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = "Товар удален",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 20.dp
                                ),
                            text = (deleteResult as DeleteResult.Success).message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    null -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(),
                            text = "Товар уже добавлен",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 20.dp
                                ),
                            text = "Штрихкод этого товара уже зарегистрирован на складе. Вы можете просмотреть его данные ниже.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        ProductItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 10.dp
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            product = product,
                            onClick = { product ->
                                onDetailsClick(product.barcode)
                            }
                        )

                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 16.dp)
                        ) {
                            TextButton(
                                modifier = Modifier
                                    .weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                onClick = {
                                    showDeleteDialog = true
                                }
                            ) {
                                Text(
                                    text = "Удалить"
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            FilledTonalButton(
                                modifier = Modifier
                                    .weight(1f),
                                onClick = {
                                    onDetailsClick(product.barcode)
                                }
                            ) {
                                Text(
                                    text = "Показать"
                                )
                            }
                        }
                    }
                }
            }
        }


        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удаление товара") },
                text = { Text("Вы уверены, что хотите удалить этот товар? Это действие нельзя будет отменить.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false

                        onProductDeleteClick(product.barcode)
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@Composable
fun StorageAddProductScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    onDetailsClick: (String) -> Unit
) {
    val addProductState by viewModel.addProductResult.collectAsState()
    val deleteProductResult by viewModel.deleteProductResult.collectAsState()
    val productState by viewModel.productState.collectAsState()

    StorageAddProductContent(
        addProductState = addProductState,
        deleteProductResult = deleteProductResult,
        productState = productState,
        onBarcodeScanned = viewModel::getProductByBarcode,
        onAddNewProductClick = viewModel::addProduct,
        onDetailsClick = onDetailsClick,
        onProductDeleteClick = viewModel::deleteProduct,
        resetStates = viewModel::resetState
    )
}


@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun StorageAddScreenPreview() {
    StorageAddProductContent(
        addProductState = UISetState.Idle,
        deleteProductResult = UISetState.Idle,
        productState = UIGetState.Empty,
        onBarcodeScanned = {},
        onAddNewProductClick = {},
        onDetailsClick = {},
        onProductDeleteClick = {},
        resetStates = {}
    )
}
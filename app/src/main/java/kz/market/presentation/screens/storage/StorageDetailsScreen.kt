package kz.market.presentation.screens.storage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kz.market.R
import kz.market.domain.models.ProductDetailsArgs
import kz.market.domain.models.ProductInputState
import kz.market.presentation.components.CameraViewfinderOverlay
import kz.market.presentation.components.camera.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDetailsScreen(
    args: ProductDetailsArgs
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

    val showPermissionDeniedDialog =
        !hasPermission &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    permission
                )


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            launcher.launch(permission)
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    val inputState = remember { mutableStateOf(
        ProductInputState(
            barcode = args.barcode,
            name = args.name,
            price = args.price.toString(),
            ownPrice = args.ownPrice.toString(),
            quantity = args.quantity.toString(),
            unit = args.unit
        )
    ) }

    var showDialog by remember { mutableStateOf(false) }
    var showDropDown by remember { mutableStateOf(false) }
    val options = mapOf(
        "Килограмм" to "кг",
        "Штука" to "шт",
        "Литр" to "л"
    )
    var showInputError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Детализация товара")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
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
                        text = """ Перед обновлением данных товара пожалуйста, убедитесь, что:
    • Название товара не пустое
    • Штрих-код введён правильно
    • Цена больше 0 (в тенге)
    • Количество больше 0
                    """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                }
            }

            item {
                val selectedLabel = options.entries.find { it.value == inputState.value.unit }?.key
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
                    onValueChange = { inputState.value = inputState.value.copy(barcode = it) },
                    label = { Text("Штрих-код товара") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (!hasPermission) {
                                    if (showPermissionDeniedDialog) {
                                        Toast.makeText(context, "Пожалуйста, предоставьте разрешение на камеру", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }

                                        context.startActivity(intent)
                                    } else {
                                        launcher.launch(permission)
                                    }

                                } else {
                                    showBottomSheet = true
                                }
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
                    value = inputState.value.price,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                            inputState.value = inputState.value.copy(price = it)
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
                    value = inputState.value.ownPrice,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                            inputState.value = inputState.value.copy(ownPrice = it)
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
                        options.forEach { (label, value) ->
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
                    value = inputState.value.quantity,
                    onValueChange = {
                        if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                            inputState.value = inputState.value.copy(quantity = it)
                        }
                    },
                    label = { Text("Остаток товара на складе") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    onClick = {
                        if (inputState.value.isNotValid()) {
                            showInputError = true
                            return@OutlinedButton
                        }

                        showInputError = false

                        val product = inputState.value.toProduct()
                        product.updatedAt = System.currentTimeMillis()
                    }
                ) {
                    Text(
                        text = "Обновить данные товара"
                    )
                }
            }

            item {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    ),
                    border = BorderStroke(1.dp, Color.Red),
                    onClick = {
                        showDialog = true
                    }
                ) {
                    Text(
                        text = "Удалить товар"
                    )
                }
            }
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Удаление товара") },
                text = { Text("Вы уверены, что хотите удалить этот товар? Это действие нельзя будет отменить.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
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
                            inputState.value = inputState.value.copy(barcode = barcode)
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


@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun StorageDetailsScreenPreview() {
    StorageDetailsScreen(
        ProductDetailsArgs(
            barcode = "123456789",
            name = "Test Product",
            price = 100.0,
            ownPrice = 50.0,
            quantity = 10,
            unit = "кг"
        )
    )
}
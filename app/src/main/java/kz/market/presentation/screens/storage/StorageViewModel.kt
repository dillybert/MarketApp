package kz.market.presentation.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.market.domain.models.Product
import kz.market.domain.usecases.product.AddProductUseCase
import kz.market.domain.usecases.product.DeleteProductByBarcodeUseCase
import kz.market.domain.usecases.product.GetAllProductsUseCase
import kz.market.domain.usecases.product.GetProductByBarcodeUseCase
import kz.market.domain.usecases.product.UpdateProductUseCase
import kz.market.utils.UIGetState
import kz.market.utils.UISetState
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getProductsUseCase: GetAllProductsUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductByBarcodeUseCase: DeleteProductByBarcodeUseCase
) : ViewModel() {

    private val _searchQueryForProducts = MutableStateFlow("")
    val searchQueryForProducts: StateFlow<String> = _searchQueryForProducts.asStateFlow()

    private val _productsState = MutableStateFlow<UIGetState<List<Product>>>(UIGetState.Loading)

    val filteredProductsState: StateFlow<UIGetState<List<Product>>> = combine(
        _productsState,
        _searchQueryForProducts
    ) { state, queries ->
        when (state) {
            is UIGetState.Success -> {
                val filteredProducts = state.data.filter { product ->
                    product.name.contains(queries, ignoreCase = true) ||
                            product.barcode.contains(queries)
                }
                UIGetState.Success(filteredProducts)
            }

            else -> state
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, UIGetState.Loading)

    private val _addProductResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val addProductResult: StateFlow<UISetState> = _addProductResult

    private val _updateProductResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val updateProductResult: StateFlow<UISetState> = _updateProductResult

    private val _deleteProductResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val deleteProductResult: StateFlow<UISetState> = _deleteProductResult

    private val _productState = MutableStateFlow<UIGetState<Product>>(UIGetState.Loading)
    val productState: StateFlow<UIGetState<Product>> = _productState.asStateFlow()

    init {
        observeAllProducts()
    }

    private fun observeAllProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .onStart { _productsState.value = UIGetState.Loading }
                .catch { e ->
                    _productsState.value = UIGetState.Error(e.message ?: "Unknown error")
                }
                .collect { products ->
                    _productsState.value = if (products.isEmpty()) UIGetState.Empty else UIGetState.Success(products)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQueryForProducts.value = query
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _addProductResult.value = UISetState.Loading

            val result = addProductUseCase(product)
            _addProductResult.value = when {
                result.isSuccess -> UISetState.Success
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _updateProductResult.value = UISetState.Loading

            val result = updateProductUseCase(product)
            _updateProductResult.value = when {
                result.isSuccess -> UISetState.Success
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deleteProduct(barcode: String) {
        viewModelScope.launch {
            _deleteProductResult.value = UISetState.Loading

            val result = deleteProductByBarcodeUseCase(barcode)
            _deleteProductResult.value = when {
                result.isSuccess -> {
                    _productState.value = UIGetState.Empty
                    UISetState.Success
                }
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun getProductByBarcode(barcode: String) {
        viewModelScope.launch {
            _productState.value = UIGetState.Loading

            val result = getProductByBarcodeUseCase(barcode)
            _productState.value = when {
                result.isSuccess -> {
                    val product = result.getOrNull()

                    if (product != null) {
                        UIGetState.Success(product)
                    } else {
                        UIGetState.Empty
                    }
                }
                else -> UIGetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _addProductResult.value = UISetState.Idle
        _updateProductResult.value = UISetState.Idle
        _deleteProductResult.value = UISetState.Idle
    }
}
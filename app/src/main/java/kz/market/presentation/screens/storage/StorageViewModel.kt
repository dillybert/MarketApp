package kz.market.presentation.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kz.market.domain.models.Product
import kz.market.domain.models.SupplierSuggestion
import kz.market.domain.usecases.product.AddProductUseCase
import kz.market.domain.usecases.product.DeleteProductByBarcodeUseCase
import kz.market.domain.usecases.product.GetAllProductsUseCase
import kz.market.domain.usecases.product.GetProductByBarcodeUseCase
import kz.market.domain.usecases.product.UpdateProductUseCase
import kz.market.domain.usecases.supplier.suggestion.AddSuggestionUseCase
import kz.market.domain.usecases.supplier.suggestion.DeleteSuggestionUseCase
import kz.market.domain.usecases.supplier.suggestion.GetAllSuggestionsUseCase
import kz.market.domain.usecases.supplier.suggestion.UpdateSuggestionUseCase
import kz.market.presentation.utils.ProductFilter
import kz.market.utils.UIGetState
import kz.market.utils.UISetState
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getProductsUseCase: GetAllProductsUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val deleteProductByBarcodeUseCase: DeleteProductByBarcodeUseCase,

    private val getAllSupplierSuggestionsUseCase: GetAllSuggestionsUseCase,
    private val addSupplierSuggestionUseCase: AddSuggestionUseCase,
    private val updateSupplierSuggestionUseCase: UpdateSuggestionUseCase,
    private val deleteSupplierSuggestionUseCase: DeleteSuggestionUseCase
) : ViewModel() {

    private val _searchQueryForProducts = MutableStateFlow("")
    val searchQueryForProducts: StateFlow<String> = _searchQueryForProducts.asStateFlow()

    private val _searchFilterForProducts = MutableStateFlow<ProductFilter>(ProductFilter.All)
    val searchFilterForProducts: StateFlow<ProductFilter> = _searchFilterForProducts.asStateFlow()

    private val _productsState = MutableStateFlow<UIGetState<List<Product>>>(UIGetState.Loading)

    @OptIn(FlowPreview::class)
    val filteredProductsState: StateFlow<UIGetState<List<Product>>> = combine(
        _productsState,
        searchQueryForProducts.debounce(500),
        _searchFilterForProducts
    ) { state, queries, filter ->
        when (state) {
            is UIGetState.Success -> {
                val filteredProducts = state.data
                    .filter { product ->
                        product.name.contains(queries, ignoreCase = true) ||
                                product.barcode.contains(queries)
                    }
                    .filter { product ->
                        when(filter) {
                            ProductFilter.All -> true
                            ProductFilter.OutOfStock -> product.quantity <= 5
                            ProductFilter.LastUpdated -> product.updatedAt != null
                            ProductFilter.LastAdded -> product.createdAt != null
                            ProductFilter.Price -> product.price != null
                        }
                    }
                    .let { products ->
                        when (filter) {
                            ProductFilter.LastUpdated -> products.sortedByDescending { it.updatedAt }
                            ProductFilter.LastAdded -> products.sortedByDescending { it.createdAt }
                            ProductFilter.Price -> products.sortedBy { it.price }
                            else -> products
                        }
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



    private val _supplierSuggestionsState =
        MutableStateFlow<UIGetState<List<SupplierSuggestion>>>(UIGetState.Loading)
    val supplierSuggestionsState: StateFlow<UIGetState<List<SupplierSuggestion>>> = _supplierSuggestionsState.asStateFlow()

    private val _addSupplierSuggestionResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val addSupplierSuggestionResult: StateFlow<UISetState> = _addSupplierSuggestionResult

    private val _updateSupplierSuggestionResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val updateSupplierSuggestionResult: StateFlow<UISetState> = _updateSupplierSuggestionResult

    private val _deleteSupplierSuggestionResult = MutableStateFlow<UISetState>(UISetState.Idle)
    val deleteSupplierSuggestionResult: StateFlow<UISetState> = _deleteSupplierSuggestionResult

    init {
        observeAllProducts()
        observeAllSupplierSuggestions()
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

    fun onCategoryFilterChange(filter: ProductFilter) {
        _searchFilterForProducts.value = filter
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


    fun observeAllSupplierSuggestions() {
        viewModelScope.launch {
            getAllSupplierSuggestionsUseCase()
                .onStart { _supplierSuggestionsState.value = UIGetState.Loading }
                .catch { e ->
                    _supplierSuggestionsState.value = UIGetState.Error(e.message ?: "Unknown error")
                }
                .collect { suggestions ->
                    _supplierSuggestionsState.value = if (suggestions.isEmpty()) UIGetState.Empty else UIGetState.Success(suggestions)
                }
        }
    }

    fun addSupplierSuggestion(suggestion: SupplierSuggestion) {
        viewModelScope.launch {
            _addSupplierSuggestionResult.value = UISetState.Loading

            val result = addSupplierSuggestionUseCase(suggestion)
            _addSupplierSuggestionResult.value = when {
                result.isSuccess -> UISetState.Success
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateSupplierSuggestion(suggestion: SupplierSuggestion) {
        viewModelScope.launch {
            _updateSupplierSuggestionResult.value = UISetState.Loading

            val result = updateSupplierSuggestionUseCase(suggestion)
            _updateSupplierSuggestionResult.value = when {
                result.isSuccess -> UISetState.Success
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun deleteSupplierSuggestion(id: String) {
        viewModelScope.launch {
            _deleteSupplierSuggestionResult.value = UISetState.Loading

            val result = deleteSupplierSuggestionUseCase(id)
            _deleteSupplierSuggestionResult.value = when {
                result.isSuccess -> UISetState.Success
                else -> UISetState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _addProductResult.value = UISetState.Idle
        _updateProductResult.value = UISetState.Idle
        _deleteProductResult.value = UISetState.Idle

        _addSupplierSuggestionResult.value = UISetState.Idle
        _updateSupplierSuggestionResult.value = UISetState.Idle
        _deleteSupplierSuggestionResult.value = UISetState.Idle
    }
}
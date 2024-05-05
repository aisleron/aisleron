package com.aisleron.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val getProductUseCase: GetProductUseCase
) : ViewModel() {
    val productName: String? get() = product?.name

    private var _inStock: Boolean = false
    val inStock: Boolean get() = _inStock

    private var product: Product? = null

    private val _productUiState = MutableStateFlow<ProductUiState>(ProductUiState.Empty)
    val productUiState: StateFlow<ProductUiState> = _productUiState


    fun hydrate(productId: Int, inStock: Boolean) {
        viewModelScope.launch {
            _productUiState.value = ProductUiState.Loading
            product = getProductUseCase(productId)
            _inStock = product?.inStock ?: inStock
            _productUiState.value = ProductUiState.Updated(this@ProductViewModel)
        }
    }

    fun saveProduct(name: String, inStock: Boolean) {
        viewModelScope.launch {
            _productUiState.value = ProductUiState.Loading
            if (product == null) {
                addProduct(name, inStock)
            } else {
                updateProduct(name, inStock)
            }
            _productUiState.value = ProductUiState.Success
        }
    }

    private suspend fun updateProduct(name: String, inStock: Boolean) {
        product!!.let {
            it.name = name
            it.inStock = inStock
        }
        updateProductUseCase(product!!)
    }

    private suspend fun addProduct(name: String, inStock: Boolean) {
        val id = addProductUseCase(
            Product(
                name = name,
                inStock = inStock,
                id = 0
            )
        )
        hydrate(id, _inStock)
    }

    sealed class ProductUiState {
        data object Empty : ProductUiState()
        data object Loading : ProductUiState()
        data object Error : ProductUiState()
        data object Success : ProductUiState()
        data class Updated(val product: ProductViewModel) : ProductUiState()
    }

}
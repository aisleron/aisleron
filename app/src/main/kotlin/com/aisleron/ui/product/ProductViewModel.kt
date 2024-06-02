package com.aisleron.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    val productName: String? get() = product?.name

    private var _inStock: Boolean = false
    val inStock: Boolean get() = _inStock

    private var product: Product? = null

    private val _productUiState = MutableStateFlow<ProductUiState>(ProductUiState.Empty)
    val productUiState: StateFlow<ProductUiState> = _productUiState


    fun hydrate(productId: Int, inStock: Boolean) {
        coroutineScope.launch {
            _productUiState.value = ProductUiState.Loading
            product = getProductUseCase(productId)
            _inStock = product?.inStock ?: inStock
            _productUiState.value = ProductUiState.Updated(this@ProductViewModel)
        }
    }

    fun saveProduct(name: String, inStock: Boolean) {
        coroutineScope.launch {
            _productUiState.value = ProductUiState.Loading
            try {
                product?.let { updateProduct(it, name, inStock) } ?: addProduct(name, inStock)
                _productUiState.value = ProductUiState.Success
            } catch (e: AisleronException) {
                _productUiState.value = ProductUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _productUiState.value =
                    ProductUiState.Error(AisleronException.GENERIC_EXCEPTION, e.message)
            }
        }
    }

    private suspend fun updateProduct(product: Product, name: String, inStock: Boolean) {
        val updateProduct = product.copy(name = name, inStock = inStock)
        updateProductUseCase(updateProduct)
        hydrate(updateProduct.id, updateProduct.inStock)
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
        data object Success : ProductUiState()
        data class Error(val errorCode: String, val errorMessage: String?) : ProductUiState()
        data class Updated(val product: ProductViewModel) : ProductUiState()
    }

}
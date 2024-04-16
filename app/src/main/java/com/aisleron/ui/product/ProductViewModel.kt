package com.aisleron.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import kotlinx.coroutines.launch

class ProductViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val getProductUseCase: GetProductUseCase
) : ViewModel() {
    val id: Int? get() = product?.id
    val productName: String? get() = product?.name
    val inStock: Boolean? get() = product?.inStock

    private var product: Product? = null

    fun hydrate(productId: Int) {
        viewModelScope.launch {
            product = getProductUseCase(productId)
        }
    }

    fun saveProduct(name: String, inStock: Boolean) {
        if (product == null) {
            addProduct(name, inStock)
        } else {
            updateProduct(name, inStock)
        }

    }

    private fun updateProduct(name: String, inStock: Boolean) {
        viewModelScope.launch {
            product!!.let {
                it.name = name
                it.inStock = inStock
            }
            updateProductUseCase(product!!)
        }
    }

    private fun addProduct(name: String, inStock: Boolean) {
        viewModelScope.launch {
            val id = addProductUseCase(
                Product(
                    name = name,
                    inStock = inStock,
                    id = 0
                )
            )
            hydrate(id)
        }
    }

}
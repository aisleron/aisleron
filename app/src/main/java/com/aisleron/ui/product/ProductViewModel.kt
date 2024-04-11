package com.aisleron.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    val id: Int? get() = product?.id
    val productName: String? get() = product?.name
    val inStock: Boolean? get() = product?.inStock

    private var product: Product? = null

    fun hydrate(productId: Int) {
        viewModelScope.launch() {
            product = repository.get(productId)
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
        viewModelScope.launch() {
            product!!.let {
                it.name = name
                it.inStock = inStock
            }
            repository.update(product!!)
        }
    }

    private fun addProduct(name: String, inStock: Boolean) {
        viewModelScope.launch() {
            val id = repository.add(
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
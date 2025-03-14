/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
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
    private val getAisleUseCase: GetAisleUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel() {
    private var _aisleId: Int? = null
    private var _locationId: Int? = null
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope
    val productName: String? get() = product?.name

    private var _inStock: Boolean = false
    val inStock: Boolean get() = _inStock

    private var product: Product? = null

    private val _productUiState = MutableStateFlow<ProductUiState>(ProductUiState.Empty)
    val productUiState: StateFlow<ProductUiState> = _productUiState


    fun hydrate(productId: Int, inStock: Boolean, locationId: Int? = null, aisleId: Int? = null) {
        coroutineScope.launch {
            _locationId = locationId
            _aisleId = aisleId
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
                val aisle = _aisleId?.let { getAisleUseCase(it) }
                product?.let { updateProduct(it, name, inStock) }
                    ?: addProduct(name, inStock, aisle)

                _productUiState.value = ProductUiState.Success
            } catch (e: AisleronException) {
                _productUiState.value = ProductUiState.Error(e.exceptionCode, e.message)
            } catch (e: Exception) {
                _productUiState.value =
                    ProductUiState.Error(
                        AisleronException.ExceptionCode.GENERIC_EXCEPTION, e.message
                    )
            }
        }
    }

    private suspend fun updateProduct(product: Product, name: String, inStock: Boolean) {
        val updateProduct = product.copy(name = name, inStock = inStock)
        updateProductUseCase(updateProduct)
        hydrate(updateProduct.id, updateProduct.inStock)
    }

    private suspend fun addProduct(name: String, inStock: Boolean, aisle: Aisle?) {
        val id = addProductUseCase(
            Product(
                name = name,
                inStock = inStock,
                id = 0
            ),
            aisle
        )
        hydrate(id, _inStock)
    }

    sealed class ProductUiState {
        data object Empty : ProductUiState()
        data object Loading : ProductUiState()
        data object Success : ProductUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ProductUiState()

        data class Updated(val product: ProductViewModel) : ProductUiState()
    }

}
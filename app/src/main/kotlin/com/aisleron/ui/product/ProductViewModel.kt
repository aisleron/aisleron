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
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.ui.note.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val getProductUseCase: GetProductUseCase,
    private val getAisleUseCase: GetAisleUseCase,
    private val applyNoteChangesUseCase: ApplyNoteChangesUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel(), NoteViewModel {
    private var _aisleId: Int? = null
    private var _locationId: Int? = null
    private var product: Product? = null
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val _uiData = MutableStateFlow(ProductUiData())
    val uiData: StateFlow<ProductUiData> = _uiData

    private val _productUiState = MutableStateFlow<ProductUiState>(ProductUiState.Empty)
    val productUiState: StateFlow<ProductUiState> = _productUiState

    override val noteFlow: StateFlow<String>
        get() = _uiData.map { it.noteText }.stateIn(
            coroutineScope,
            SharingStarted.Lazily,
            _uiData.value.noteText
        )

    private var hydrated = false

    fun hydrate(productId: Int, inStock: Boolean, locationId: Int? = null, aisleId: Int? = null) {
        if (hydrated) return
        hydrated = true

        coroutineScope.launch {
            _locationId = locationId
            _aisleId = aisleId
            _productUiState.value = ProductUiState.Loading
            product = getProductUseCase(productId)
            _uiData.value = ProductUiData(
                productName = product?.name.orEmpty(),
                inStock = product?.inStock ?: inStock,
                noteText = product?.note?.noteText ?: ""
            )
            _productUiState.value = ProductUiState.Empty
        }
    }

    fun updateProductName(name: String) {
        _uiData.value = _uiData.value.copy(productName = name)
    }

    fun updateInStock(inStock: Boolean) {
        _uiData.value = _uiData.value.copy(inStock = inStock)
    }

    override fun updateNote(note: String) {
        _uiData.value = _uiData.value.copy(noteText = note)
    }

    fun saveProduct() {
        coroutineScope.launch {
            val name = _uiData.value.productName
            val inStock = _uiData.value.inStock
            val noteText = _uiData.value.noteText
            if (name.isBlank()) return@launch

            _productUiState.value = ProductUiState.Loading
            try {
                val aisle = _aisleId?.let { getAisleUseCase(it) }
                product?.let {
                    val updated = it.copy(name = name, inStock = inStock)
                    updateProductUseCase(updated)
                    product = updated
                } ?: run {
                    val id = addProductUseCase(
                        Product(
                            name = name,
                            inStock = inStock,
                            id = 0,
                            qtyNeeded = 0
                        ),
                        aisle
                    )

                    product = getProductUseCase(id)
                }

                product?.let {
                    val note = Note(it.noteId ?: 0, noteText)
                    applyNoteChangesUseCase(it, note)
                }

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

    sealed class ProductUiState {
        data object Empty : ProductUiState()
        data object Loading : ProductUiState()
        data object Success : ProductUiState()
        data class Error(
            val errorCode: AisleronException.ExceptionCode, val errorMessage: String?
        ) : ProductUiState()
    }

}
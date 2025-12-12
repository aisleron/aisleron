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
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.aisleproduct.usecase.ChangeProductAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.note.Note
import com.aisleron.domain.note.usecase.ApplyNoteChangesUseCase
import com.aisleron.domain.note.usecase.GetNoteParentUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCase
import com.aisleron.domain.product.usecase.GetProductMappingsUseCase
import com.aisleron.domain.product.usecase.UpdateProductUseCase
import com.aisleron.ui.bundles.AisleListEntry
import com.aisleron.ui.bundles.AislePickerBundle
import com.aisleron.ui.note.NoteParentRef
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
    private val getAisleUseCase: GetAisleUseCase,
    private val getNoteParentUseCase: GetNoteParentUseCase,
    private val applyNoteChangesUseCase: ApplyNoteChangesUseCase,
    private val getProductMappingsUseCase: GetProductMappingsUseCase,
    private val getAislesForLocationUseCase: GetAislesForLocationUseCase,
    private val changeProductAisleUseCase: ChangeProductAisleUseCase,
    coroutineScopeProvider: CoroutineScope? = null
) : ViewModel(), NoteViewModel {
    private var _aisleId: Int? = null
    private var _locationId: Int? = null
    private var product: Product? = null
    private val coroutineScope = coroutineScopeProvider ?: this.viewModelScope

    private val _uiData = MutableStateFlow(ProductUiData())
    val uiData: StateFlow<ProductUiData> = _uiData

    private val _productAisles = MutableStateFlow<List<ProductAisleInfo>>(emptyList())
    val productAisles: StateFlow<List<ProductAisleInfo>> = _productAisles

    private val _productUiState = MutableStateFlow<ProductUiState>(ProductUiState.Empty)
    val productUiState: StateFlow<ProductUiState> = _productUiState

    private val _aislesForLocation = MutableStateFlow<AislePickerBundle?>(null)
    val aislesForLocation: StateFlow<AislePickerBundle?> = _aislesForLocation

    private var editingAisleInfo: ProductAisleInfo? = null

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
            product = getProduct(productId)
            _uiData.value = ProductUiData(
                productName = product?.name.orEmpty(),
                inStock = product?.inStock ?: inStock,
                noteText = product?.note?.noteText ?: ""
            )
            product?.let { p ->
                _productAisles.value = getProductMappingsUseCase(p.id).flatMap { location ->
                    location.aisles.map { aisle ->
                        ProductAisleInfo(
                            locationId = location.id,
                            locationName = location.name,
                            aisleId = aisle.id,
                            aisleName = aisle.name,
                            initialAisleId = aisle.id
                        )
                    }
                }
            }

            // TODO: Load locations that don't have the product in them
            _productUiState.value = ProductUiState.Empty
        }
    }

    fun requestLocationAisles(item: ProductAisleInfo) {
        editingAisleInfo = item
        coroutineScope.launch {
            val aisles = getAislesForLocationUseCase(item.locationId)
                .sortedBy { it.rank }
                .map { AisleListEntry(it.id, it.name) }

            _aislesForLocation.value = AislePickerBundle(
                title = item.locationName,
                aisles = aisles,
                currentAisleId = item.aisleId
            )
        }
    }

    fun onAislesDialogShown() {
        _aislesForLocation.value = null
    }

    fun updateProductName(name: String) {
        _uiData.value = _uiData.value.copy(productName = name)
    }

    fun updateInStock(inStock: Boolean) {
        _uiData.value = _uiData.value.copy(inStock = inStock)
    }

    override fun updateNote(noteText: String) {
        _uiData.value = _uiData.value.copy(noteText = noteText)
    }

    private suspend fun getProduct(productId: Int): Product? {
        return getNoteParentUseCase(NoteParentRef.Product(productId)) as Product?
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

                    product = getProduct(id)
                }

                product?.let { p ->
                    val note = Note(p.noteId ?: 0, noteText)
                    applyNoteChangesUseCase(p, note)

                    val aisles = _productAisles.value
                    aisles.filter { it.aisleId != it.initialAisleId }.forEach {
                        changeProductAisleUseCase(p.id, it.initialAisleId, it.aisleId)
                    }
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

    fun updateProductAisle(selectedAisleId: Int) {
        val selectedAisleInfo = editingAisleInfo ?: return

        coroutineScope.launch {
            getAisleUseCase(selectedAisleId)?.let { a ->
                val currentAisles = _productAisles.value
                _productAisles.value = currentAisles.map {
                    if (it == selectedAisleInfo) {
                        it.copy(aisleId = a.id, aisleName = a.name)
                    } else {
                        it
                    }
                }
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

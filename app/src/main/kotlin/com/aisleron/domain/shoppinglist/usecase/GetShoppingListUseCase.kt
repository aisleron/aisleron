/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.note.usecase.GetNotesUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

interface GetShoppingListUseCase {
    operator fun invoke(locationId: Int, filter: ShoppingListFilter): Flow<Location?>
}

class GetShoppingListUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val getNotesUseCase: GetNotesUseCase
) : GetShoppingListUseCase {
    @OptIn(ExperimentalCoroutinesApi::class)
    override operator fun invoke(locationId: Int, filter: ShoppingListFilter): Flow<Location?> {
        return locationRepository.getLocationWithAislesWithProducts(locationId)
            .flatMapLatest { location ->
                if (location == null) return@flatMapLatest flowOf(null)

                val productNameQuery = filter.productNameQuery.trim()
                val productFilter =
                    resolveProductFilter(productNameQuery, filter.productFilter, location)

                val aislesWithFilteredProducts = location.aisles.map { aisle ->
                    aisle.copy(
                        products = aisle.products.filter { ap ->
                            isValidAisleProduct(ap, productFilter, productNameQuery)
                        }.sortedWith(compareBy<AisleProduct> { it.rank }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.product.name }
                        )
                    )
                }

                val filteredAisles = aislesWithFilteredProducts.filter { aisle ->
                    isValidAisle(
                        aisle, location.showDefaultAisle, productNameQuery, filter.showEmptyAisles
                    )
                }.sortedWith(compareBy<Aisle> { it.rank }
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                )

                val noteIds = filteredAisles
                    .flatMap { it.products }
                    .mapNotNull { it.product.noteId }
                    .distinct()

                //  Combine the location flow with the notes flow
                combine(
                    flowOf(filteredAisles),
                    getNotesUseCase(noteIds)
                ) { aisles, notes ->
                    val notesMap = notes.associateBy { it.id }

                    // Map notes into the structure
                    val hydratedAisles = aisles.map { aisle ->
                        aisle.copy(
                            products = aisle.products.map { ap ->
                                ap.copy(product = ap.product.copy(note = notesMap[ap.product.noteId]))
                            }
                        )
                    }

                    location.copy(aisles = hydratedAisles)
                }
            }
    }

    private fun resolveProductFilter(
        productNameQuery: String, productFilter: FilterType?, location: Location
    ): FilterType = if (productNameQuery.isNotBlank())
        FilterType.ALL
    else
        productFilter ?: location.defaultFilter

    private fun isValidAisle(
        aisle: Aisle, showDefaultAisle: Boolean, productNameQuery: String, showEmptyAisles: Boolean
    ): Boolean {
        val hasValidProducts = aisle.products.isNotEmpty()

        val showDefault = showDefaultAisle || productNameQuery.isNotBlank()

        return (showDefault || !aisle.isDefault) && (hasValidProducts || showEmptyAisles)
    }

    private fun isValidAisleProduct(
        aisleProduct: AisleProduct, productFilter: FilterType, productNameQuery: String
    ): Boolean {
        val matchesFilter = when (productFilter) {
            FilterType.IN_STOCK -> aisleProduct.product.inStock
            FilterType.NEEDED -> !aisleProduct.product.inStock
            FilterType.ALL -> true
        }

        val matchesQuery = aisleProduct.product.name.contains(productNameQuery, ignoreCase = true)

        return matchesFilter && matchesQuery
    }
}
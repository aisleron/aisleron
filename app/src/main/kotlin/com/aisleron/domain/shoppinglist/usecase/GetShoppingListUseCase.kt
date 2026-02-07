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
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.note.usecase.GetNotesUseCase
import com.aisleron.domain.shoppinglist.ShoppingListFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface GetShoppingListUseCase {
    operator fun invoke(locationId: Int, filter: ShoppingListFilter): Flow<Location?>

    operator fun invoke(
        locationType: LocationType, filter: ShoppingListFilter
    ): Flow<List<Location>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class GetShoppingListUseCaseImpl(
    private val locationRepository: LocationRepository,
    private val getNotesUseCase: GetNotesUseCase
) : GetShoppingListUseCase {
    override operator fun invoke(locationId: Int, filter: ShoppingListFilter): Flow<Location?> {
        return locationRepository.getLocationWithAislesWithProducts(locationId)
            .flatMapLatest { location ->
                if (location == null) return@flatMapLatest flowOf(null)

                applyFiltersAndHydrate(listOf(location), filter, true)
                    .map { it.firstOrNull() }
            }
    }

    override operator fun invoke(
        locationType: LocationType, filter: ShoppingListFilter
    ): Flow<List<Location>> {
        return locationRepository.getLocationsWithAislesWithProducts(locationType)
            .flatMapLatest { locations ->
                applyFiltersAndHydrate(locations, filter, false)
            }
    }

    private suspend fun applyFiltersAndHydrate(
        locations: List<Location>, filter: ShoppingListFilter, viewByAisle: Boolean
    ): Flow<List<Location>> {
        if (locations.isEmpty()) return flowOf(emptyList())

        val productNameQuery = filter.productNameQuery.trim()

        // 1. Filter Aisle/Products for every location
        val processedLocations = locations.map { location ->
            val productFilter =
                resolveProductFilter(productNameQuery, filter.productFilter)

            val filteredAisles = location.aisles.map { aisle ->
                aisle.copy(
                    products = aisle.products.filter {
                        isValidAisleProduct(it, productFilter, productNameQuery)
                    }.sortedWith(compareBy<AisleProduct> { it.rank }
                        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.product.name }
                    )
                )
            }.filter {
                isValidAisle(
                    it,
                    location.showDefaultAisle,
                    productNameQuery,
                    filter.showEmptyAisles,
                    viewByAisle
                )
            }.sortedWith(compareBy<Aisle> { it.rank }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
            )

            location.copy(aisles = filteredAisles)
        }.filter {
            isValidLocation(it, filter.showEmptyAisles)
        }.sortedWith(compareBy<Location> { it.rank }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )

        // 2. Extract all Note IDs
        val allNoteIds = processedLocations
            .flatMap { it.aisles }.flatMap { it.products }
            .mapNotNull { it.product.noteId }.distinct()

        // 3. Hydrate with Notes
        return combine(flowOf(processedLocations), getNotesUseCase(allNoteIds)) { locList, notes ->
            val notesMap = notes.associateBy { it.id }
            locList.map { loc ->
                loc.copy(aisles = loc.aisles.map { aisle ->
                    aisle.copy(products = aisle.products.map { ap ->
                        ap.copy(product = ap.product.copy(note = notesMap[ap.product.noteId]))
                    })
                })
            }
        }
    }

    private fun resolveProductFilter(
        productNameQuery: String, productFilter: FilterType
    ): FilterType = if (productNameQuery.isNotBlank())
        FilterType.ALL
    else
        productFilter

    private fun isValidLocation(location: Location, showEmptyAisles: Boolean): Boolean {
        val hasValidAisles = location.aisles.isNotEmpty()

        return (hasValidAisles || showEmptyAisles)
    }

    private fun isValidAisle(
        aisle: Aisle,
        showDefaultAisle: Boolean,
        productNameQuery: String,
        showEmptyAisles: Boolean,
        viewByAisle: Boolean
    ): Boolean {
        val hasValidProducts = aisle.products.isNotEmpty()

        val showDefault = (showDefaultAisle || productNameQuery.isNotBlank()) && viewByAisle

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
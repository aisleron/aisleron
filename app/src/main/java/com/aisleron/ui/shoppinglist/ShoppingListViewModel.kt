package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.LocationData
import com.aisleron.placeholder.ProductData

class ShoppingListViewModel(
    private val locationId: Long,
    val filterType: FilterType
) : ViewModel() {
    private val location: Location? = LocationData.locations.find { l -> l.id == locationId }
    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.GENERIC

    val items = mutableListOf<ShoppingListItemViewModel>()

    fun updateProduct(item: ShoppingListItemViewModel) {
        item.inStock?.let {
            ProductData.products.find { p -> p.id == item.id }?.inStock = it
        }
    }

    fun refreshListItems() {
        items.clear()

        location?.aisles?.forEach { a ->
            items.add(
                ShoppingListItemViewModel(
                    ShoppingListItemType.AISLE,
                    a.rank,
                    -1,
                    a.id,
                    a.name,
                    null
                )
            )
            a.products.filter { p ->
                (p.inStock && filterType == FilterType.IN_STOCK)
                        || (!p.inStock && filterType == FilterType.NEEDED)
                        || (filterType == FilterType.ALL)
            }.forEach { p ->
                items.add(
                    ShoppingListItemViewModel(
                        ShoppingListItemType.PRODUCT,
                        a.rank,
                        p.id.toInt(),
                        p.id,
                        p.name,
                        p.inStock
                    )
                )
            }
        }

        items.sortWith(compareBy({ it.aisleRank }, { it.productRank }))
        //TODO: Add AisleEntity ID and/or AisleEntity & ProductEntity items to view model list
    }
}
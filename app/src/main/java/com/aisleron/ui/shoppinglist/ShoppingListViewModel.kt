package com.aisleron.ui.shoppinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.placeholder.ProductData
import kotlinx.coroutines.launch

class ShoppingListViewModel(
    private val repository: LocationRepository,
    private val locationId: Int,
    val filterType: FilterType
) : ViewModel() {

    val location: Location? = getLocationFromRepo()


    private fun getLocationFromRepo() : Location? {
        var result: Location? = null
        viewModelScope.launch() {
            result = repository.get(locationId)
        }
        return result
    }



    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.HOME

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
                        p.id,
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
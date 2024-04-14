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

    private val location: Location? = getLocationFromRepo()

    private fun getLocationFromRepo(): Location? {
        var result: Location? = null
        viewModelScope.launch {
            result = repository.get(locationId)
        }
        return result
    }


    val locationName: String get() = location?.name.toString()
    val locationType: LocationType get() = location?.type ?: LocationType.HOME

    private val _items = mutableListOf<ShoppingListItemViewModel>()
    val items: List<ShoppingListItemViewModel> = _items

    fun updateProduct(item: ShoppingListItemViewModel) {
        item.inStock?.let {
            ProductData.products.find { p -> p.id == item.id }?.inStock = it
        }
    }

    fun refreshListItems() {
        _items.clear()

        location?.aisles?.forEach { a ->
            _items.add(
                ShoppingListItemViewModel(
                    lineItemType = ShoppingListItemType.AISLE,
                    aisleRank = a.rank,
                    productRank = -1,
                    id = a.id,
                    name = a.name,
                    inStock = null
                )
            )
            _items += a.products.filter { p ->
                (p.product.inStock && filterType == FilterType.IN_STOCK)
                        || (!p.product.inStock && filterType == FilterType.NEEDED)
                        || (filterType == FilterType.ALL)
            }.map { p ->
                ShoppingListItemViewModel(
                    lineItemType = ShoppingListItemType.PRODUCT,
                    aisleRank = a.rank,
                    productRank = p.rank,
                    id = p.product.id,
                    name = p.product.name,
                    inStock = p.product.inStock
                )
            }
        }

        _items.sortWith(compareBy({ it.aisleRank }, { it.productRank }))
        //TODO: Add Aisle Id and/or Aisle Object & Product Object items to view model list
    }

    fun removeItem(item: ShoppingListItemViewModel) {
        _items.remove(item)
    }
}
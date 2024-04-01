package com.aisleron.ui.shoppinglist
import androidx.lifecycle.ViewModel
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.Location
import com.aisleron.domain.model.LocationType
import com.aisleron.placeholder.LocationData
import java.util.Date


class ShoppingListViewModel(
    private val locationId: Long,
    var filterType: FilterType = FilterType.NEEDED
): ViewModel() {
    val locationName: String
    val items = mutableListOf<ShoppingListItemViewModel>()
    val locationType: LocationType
    val lastInit = Date()
    init {
        val location: Location? = LocationData.locations.find { l -> l.id == locationId}
        locationType = location?.type ?: LocationType.GENERIC
        locationName =  location?.name.toString()

        location?.aisles?.forEach {a ->
            items.add(ShoppingListItemViewModel(ShoppingListItemType.AISLE, a.rank, -1, a))
            a.products?.filter { p ->
                (p.inStock && filterType == FilterType.INSTOCK)
                        || (!p.inStock && filterType == FilterType.NEEDED)
                        || (filterType == FilterType.ALL)
            }?.forEach { p ->
                items.add(ShoppingListItemViewModel(ShoppingListItemType.PRODUCT, a.rank, p.id.toInt(), p))
            }
        }
    }



}
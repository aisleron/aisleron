package com.aisleron.ui.bundles

import android.os.Parcelable
import com.aisleron.domain.FilterType
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShoppingListBundle(
    val locationId: Int,
    val filterType: FilterType
) : Parcelable {
    companion object {
        operator fun invoke(locationId: Int?, filterType: FilterType?) =
            ShoppingListBundle(locationId ?: 1, filterType ?: FilterType.ALL)
    }
}

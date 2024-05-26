package com.aisleron.ui.bundles

import android.os.Parcelable
import com.aisleron.domain.location.LocationType
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddEditLocationBundle(
    val locationId: Int = 0,
    val name: String? = null,
    val locationType: LocationType = LocationType.SHOP,
    val actionType: LocationAction = LocationAction.ADD
) : Parcelable {
    enum class LocationAction {
        ADD, EDIT
    }
}

/*
 */

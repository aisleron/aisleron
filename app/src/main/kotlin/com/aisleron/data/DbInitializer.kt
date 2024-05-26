package com.aisleron.data

import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.location.LocationEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DbInitializer {
    operator fun invoke(db: AisleronDatabase) {

        val home = LocationEntity(
            id = 0,
            type = LocationType.HOME,
            defaultFilter = FilterType.NEEDED,
            name = "Home",
            pinned = false
        )
        CoroutineScope(Dispatchers.IO).launch {
            val homeId = db.locationDao().upsert(home)[0].toInt()
            val aisle = AisleEntity(
                id = 0,
                name = "No Aisle",
                locationId = homeId,
                rank = 1000,
                isDefault = true
            )

            db.aisleDao().upsert(aisle)
        }

    }
}
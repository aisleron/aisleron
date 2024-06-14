package com.aisleron.data

import com.aisleron.data.aisle.AisleEntity
import com.aisleron.data.location.LocationEntity
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DbInitializer(
    private val database: AisleronDatabase,
    coroutineScopeProvider: CoroutineScope? = null
) {
    private val coroutineScope = coroutineScopeProvider ?: CoroutineScope(Dispatchers.IO)

    operator fun invoke() {

        val home = LocationEntity(
            id = 0,
            type = LocationType.HOME,
            defaultFilter = FilterType.NEEDED,
            name = "Home",
            pinned = false
        )
        coroutineScope.launch {
            val homeId = database.locationDao().upsert(home)[0].toInt()
            val aisle = AisleEntity(
                id = 0,
                name = "No Aisle",
                locationId = homeId,
                rank = 1000,
                isDefault = true
            )

            database.aisleDao().upsert(aisle)
        }

    }
}
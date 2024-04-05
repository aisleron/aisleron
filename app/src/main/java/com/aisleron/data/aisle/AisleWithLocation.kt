package com.aisleron.data.aisle

import androidx.room.Embedded
import androidx.room.Relation
import com.aisleron.data.location.LocationEntity

data class AisleWithLocation(
    @Embedded val aisle: AisleEntity,
    @Relation(parentColumn = "locationId", entityColumn = "id") val location: LocationEntity
)

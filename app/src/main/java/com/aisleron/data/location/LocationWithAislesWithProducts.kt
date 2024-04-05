package com.aisleron.data.location

import androidx.room.Embedded
import androidx.room.Relation
import com.aisleron.data.aisle.AisleWithProducts

data class LocationWithAislesWithProducts(
    @Embedded val location: LocationEntity,
    @Relation(parentColumn = "id", entityColumn = "locationId") val aisles: List<AisleWithProducts>
)

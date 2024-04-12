package com.aisleron.data.aisle

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.location.Location

class AisleMapper : MapperBaseImpl<AisleEntity, Aisle>() {
    override fun toModel(value: AisleEntity) = toModel(value, null)

    override fun fromModel(value: Aisle) = AisleEntity(
        name = value.name,
        id = value.id,
        rank = value.rank,
        locationId = value.location?.id ?: 0
    )

    fun toModel(value: AisleEntity, location: Location?) = Aisle(
        name = value.name,
        id = value.id,
        rank = value.rank,
        location = location,
        products = emptyList()
    )
}
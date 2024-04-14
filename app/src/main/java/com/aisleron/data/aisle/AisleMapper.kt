package com.aisleron.data.aisle

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.aisle.Aisle

class AisleMapper : MapperBaseImpl<AisleEntity, Aisle>() {
    override fun toModel(value: AisleEntity) = Aisle(
        name = value.name,
        id = value.id,
        rank = value.rank,
        locationId = value.locationId,
        products = emptyList(),
        isDefault = value.isDefault
    )

    override fun fromModel(value: Aisle) = AisleEntity(
        name = value.name,
        id = value.id,
        rank = value.rank,
        locationId = value.locationId,
        isDefault = value.isDefault
    )
}
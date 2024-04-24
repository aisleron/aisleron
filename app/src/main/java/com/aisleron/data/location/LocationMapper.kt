package com.aisleron.data.location

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.location.Location

class LocationMapper : MapperBaseImpl<LocationEntity, Location>() {
    override fun toModel(value: LocationEntity) = Location(
        id = value.id,
        name = value.name.trim(),
        defaultFilter = value.defaultFilter,
        pinned = value.pinned,
        type = value.type,
        aisles = emptyList()
    )

    override fun fromModel(value: Location) = LocationEntity(
        id = value.id,
        name = value.name.trim(),
        defaultFilter = value.defaultFilter,
        pinned = value.pinned,
        type = value.type,
    )
}
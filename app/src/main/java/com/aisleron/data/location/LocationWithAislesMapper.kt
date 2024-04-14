package com.aisleron.data.location

import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.location.Location

class LocationWithAislesMapper :
    MapperBaseImpl<LocationWithAisles, Location>() {
    override fun toModel(value: LocationWithAisles): Location {
        val location = LocationMapper().toModel(value.location)
        location.aisles = AisleMapper().toModelList(value.aisles)
        return location
    }

    override fun fromModel(value: Location) = LocationWithAisles(
        location = LocationMapper().fromModel(value),
        aisles = AisleMapper().fromModelList(value.aisles)
    )
}
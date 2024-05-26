package com.aisleron.data.location

import com.aisleron.data.aisle.AisleWithProductsMapper
import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.location.Location

class LocationWithAislesWithProductsMapper :
    MapperBaseImpl<LocationWithAislesWithProducts, Location>() {
    override fun toModel(value: LocationWithAislesWithProducts): Location {
        val location = LocationMapper().toModel(value.location)
        location.aisles = AisleWithProductsMapper().toModelList(value.aisles)
        return location
    }

    override fun fromModel(value: Location) = LocationWithAislesWithProducts(
        location = LocationMapper().fromModel(value),
        aisles = AisleWithProductsMapper().fromModelList(value.aisles)
    )
}
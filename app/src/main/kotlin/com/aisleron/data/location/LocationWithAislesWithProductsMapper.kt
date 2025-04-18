/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.location

import com.aisleron.data.aisle.AisleWithProductsMapper
import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.location.Location

class LocationWithAislesWithProductsMapper :
    MapperBaseImpl<LocationWithAislesWithProducts, Location>() {
    override fun toModel(value: LocationWithAislesWithProducts): Location {
        val location = LocationMapper().toModel(value.location)
        return location.copy(aisles = AisleWithProductsMapper().toModelList(value.aisles))
    }

    override fun fromModel(value: Location) = LocationWithAislesWithProducts(
        location = LocationMapper().fromModel(value),
        aisles = AisleWithProductsMapper().fromModelList(value.aisles)
    )
}
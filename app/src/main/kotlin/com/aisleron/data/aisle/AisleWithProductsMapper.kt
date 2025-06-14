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

package com.aisleron.data.aisle

import com.aisleron.data.aisleproduct.AisleProductRankMapper
import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.aisle.Aisle

class AisleWithProductsMapper : MapperBaseImpl<AisleWithProducts, Aisle>() {
    override fun toModel(value: AisleWithProducts) = Aisle(
        id = value.aisle.id,
        rank = value.aisle.rank,
        name = value.aisle.name.trim(),
        locationId = value.aisle.locationId,
        products = AisleProductRankMapper().toModelList(value.products),
        isDefault = value.aisle.isDefault,
        expanded = value.aisle.expanded
    )

    override fun fromModel(value: Aisle) = AisleWithProducts(
        aisle = AisleMapper().fromModel(value),
        products = AisleProductRankMapper().fromModelList(value.products)
    )
}
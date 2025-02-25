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

package com.aisleron.data.aisleproduct

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.data.product.ProductMapper
import com.aisleron.domain.aisleproduct.AisleProduct

class AisleProductRankMapper : MapperBaseImpl<AisleProductRank, AisleProduct>() {
    override fun toModel(value: AisleProductRank) = AisleProduct(
        rank = value.aisleProduct.rank,
        aisleId = value.aisleProduct.aisleId,
        id = value.aisleProduct.id,
        product = ProductMapper().toModel(value.product)
    )

    override fun fromModel(value: AisleProduct) = AisleProductRank(
        aisleProduct = AisleProductEntity(
            aisleId = value.aisleId,
            rank = value.rank,
            productId = value.product.id,
            id = value.id
        ),
        product = ProductMapper().fromModel(value.product)
    )
}
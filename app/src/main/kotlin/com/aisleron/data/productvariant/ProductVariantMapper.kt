/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.productvariant

import com.aisleron.data.base.Mapper
import com.aisleron.data.base.SyncEntity
import com.aisleron.domain.productvariant.ProductVariant

class ProductVariantMapper : Mapper<ProductVariantEntity, ProductVariant> {
    override fun toModel(value: ProductVariantEntity) = ProductVariant(
        id = value.id,
        productId = value.productId,
        barcode = value.barcode,
        createdAt = value.createdAt
    )

    override fun fromModel(value: ProductVariant, syncMetadata: SyncEntity?) = ProductVariantEntity(
        id = value.id,
        productId = value.productId,
        barcode = value.barcode,
        createdAt = value.createdAt
    )
}

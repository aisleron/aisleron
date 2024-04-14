package com.aisleron.data.aisle

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.aisle.Aisle

class AisleWithProductsMapper : MapperBaseImpl<AisleWithProducts, Aisle>() {
    override fun toModel(value: AisleWithProducts) = Aisle(
        id = value.aisle.id,
        rank = value.aisle.rank,
        name = value.aisle.name,
        locationId = value.aisle.locationId,
        products = AisleProductRankMapper().toModelList(value.products),
        isDefault = value.aisle.isDefault
    )

    override fun fromModel(value: Aisle) = AisleWithProducts(
        aisle = AisleMapper().fromModel(value),
        products = AisleProductRankMapper().fromModelList(value.products)
    )
}
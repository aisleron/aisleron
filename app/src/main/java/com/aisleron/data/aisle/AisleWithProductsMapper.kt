package com.aisleron.data.aisle

import com.aisleron.data.base.MapperBaseImpl
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.location.Location

class AisleWithProductsMapper : MapperBaseImpl<AisleWithProducts, Aisle>() {
    override fun toModel(value: AisleWithProducts) = toModel(value, null)

    override fun fromModel(value: Aisle) = AisleWithProducts(
        aisle = AisleMapper().fromModel(value),
        products = AisleProductRankMapper().fromModelList(value.products)
    )

    fun toModel(value: AisleWithProducts, location: Location?) = Aisle(
        id = value.aisle.id,
        rank = value.aisle.rank,
        location = location,
        name = value.aisle.name,
        products = AisleProductRankMapper().toModelList(value.products)
    )

    override fun toModelList(list: List<AisleWithProducts>) = toModelList(list, null)

    fun toModelList(list: List<AisleWithProducts>, location: Location?): List<Aisle> {
        return list.map { toModel(it, location) }
    }
}
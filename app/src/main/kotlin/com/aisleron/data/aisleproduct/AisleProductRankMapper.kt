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
package com.aisleron.data.aisleproduct

import androidx.room.Embedded
import androidx.room.Relation
import com.aisleron.data.product.ProductEntity

data class AisleProductRank(
    @Embedded val aisleProduct: AisleProductEntity,
    @Relation(parentColumn = "productId", entityColumn = "id") val product: ProductEntity
)

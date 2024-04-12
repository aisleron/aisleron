package com.aisleron.domain.aisle

import com.aisleron.domain.product.Product

data class AisleProduct(
    var rank: Int,
    var aisleId: Int,
    val product: Product
)

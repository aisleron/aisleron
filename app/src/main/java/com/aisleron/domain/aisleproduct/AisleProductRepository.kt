package com.aisleron.domain.aisleproduct

import com.aisleron.domain.base.BaseRepository

interface AisleProductRepository : BaseRepository<AisleProduct> {
    suspend fun addAisleProducts(aisleProducts: List<AisleProduct>)
}
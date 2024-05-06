package com.aisleron.domain.aisleproduct

import com.aisleron.domain.base.BaseRepository

interface AisleProductRepository : BaseRepository<AisleProduct> {
    suspend fun updateAisleProductRank(item: AisleProduct)
    suspend fun removeProductsFromAisle(aisleId: Int)
}
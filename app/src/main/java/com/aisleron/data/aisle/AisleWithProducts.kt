package com.aisleron.data.aisle

import androidx.room.Embedded
import androidx.room.Relation

data class AisleWithProducts(
    @Embedded val aisle: AisleEntity,
    @Relation(
        entity = AisleProductEntity::class,
        parentColumn = "id",
        entityColumn = "aisleId",
    ) val products: List<AisleProductRank>
)

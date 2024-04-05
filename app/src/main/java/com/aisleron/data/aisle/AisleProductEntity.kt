package com.aisleron.data.aisle

import androidx.room.Entity

@Entity(tableName = "AisleProduct", primaryKeys = ["aisleId", "productId"])
data class AisleProductEntity(
    val aisleId: Int,
    val productId: Int,
    val rank: Int
)
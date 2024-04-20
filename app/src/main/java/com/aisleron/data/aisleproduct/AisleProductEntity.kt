package com.aisleron.data.aisleproduct

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "AisleProduct",
    indices = [Index(value = ["aisleId", "productId"], unique = true)]
)
data class AisleProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val aisleId: Int,
    val productId: Int,
    val rank: Int
)
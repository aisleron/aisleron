package com.aisleron.data.product

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Product")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val inStock: Boolean
)
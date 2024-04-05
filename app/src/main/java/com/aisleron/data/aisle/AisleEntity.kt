package com.aisleron.data.aisle

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Aisle")
data class AisleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val locationId: Int,
    val rank: Int
)
package com.aisleron.data.location

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.LocationType

@Entity (tableName = "Location")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val type: LocationType,
    val defaultFilter: FilterType,
    val name: String
)

package com.aisleron.data.location

import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

interface LocationDao : BaseDao<LocationEntity> {

    /**
     * Location
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    fun getLocation(locationId: Int): LocationEntity

    @Transaction
    @Query("SELECT * FROM Location WHERE id IN (:locationId)")
    fun getLocations(vararg locationId: Int): List<LocationEntity>

    @Transaction
    @Query("SELECT * FROM Location")
    fun getLocations(): List<LocationEntity>

    /**
     * Location With Aisles
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    fun getLocationWithAisles(locationId: Int): LocationWithAisles

    @Transaction
    @Query("SELECT * FROM Location WHERE id in (:locationId)")
    fun getLocationsWithAisles(vararg locationId: Int): List<LocationWithAisles>

    @Transaction
    @Query("SELECT * FROM Location")
    fun getLocationsWithAisles(): List<LocationWithAisles>

    /**
     * Location With Aisles With Products
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    fun getLocationWithAislesWithProducts(locationId: Int): LocationWithAislesWithProducts

    @Transaction
    @Query("SELECT * FROM Location WHERE id in (:locationId)")
    fun getLocationsWithAislesWithProducts(vararg locationId: Int): List<LocationWithAislesWithProducts>

    @Transaction
    @Query("SELECT * FROM Location")
    fun getLocationsWithAislesWithProducts(): List<LocationWithAislesWithProducts>
}
package com.aisleron.data.location

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface LocationDao : BaseDao<LocationEntity> {

    /**
     * Location
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocation(locationId: Int): LocationEntity?

    @Transaction
    @Query("SELECT * FROM Location WHERE id IN (:locationId)")
    suspend fun getLocations(vararg locationId: Int): List<LocationEntity>

    @Transaction
    @Query("SELECT * FROM Location")
    suspend fun getLocations(): List<LocationEntity>

    /**
     * Location With Aisles
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocationWithAisles(locationId: Int): LocationWithAisles

    @Transaction
    @Query("SELECT * FROM Location WHERE id in (:locationId)")
    suspend fun getLocationsWithAisles(vararg locationId: Int): List<LocationWithAisles>

    @Transaction
    @Query("SELECT * FROM Location")
    suspend fun getLocationsWithAisles(): List<LocationWithAisles>

    /**
     * Location With Aisles With Products
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocationWithAislesWithProducts(locationId: Int): LocationWithAislesWithProducts?

    @Transaction
    @Query("SELECT * FROM Location WHERE id in (:locationId)")
    suspend fun getLocationsWithAislesWithProducts(vararg locationId: Int): List<LocationWithAislesWithProducts>

    @Transaction
    @Query("SELECT * FROM Location")
    suspend fun getLocationsWithAislesWithProducts(): List<LocationWithAislesWithProducts>

    /**
     * Shop Specific Queries
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE type = 'SHOP'")
    suspend fun getShops(): List<LocationEntity>

    @Transaction
    @Query("SELECT * FROM Location WHERE type = 'SHOP' AND pinned = 1")
    suspend fun getPinnedShops(): List<LocationEntity>

    /**
     * Home Specific Queries
     */
    @Transaction
    @Query("SELECT * FROM Location WHERE type = 'HOME'")
    suspend fun getHome(): LocationEntity

}
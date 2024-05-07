package com.aisleron.data.location

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao : BaseDao<LocationEntity> {

    /**
     * Location
     */
    @Query("SELECT * FROM Location WHERE id = :locationId")
    suspend fun getLocation(locationId: Int): LocationEntity?

    @Query("SELECT * FROM Location WHERE id IN (:locationId)")
    suspend fun getLocations(vararg locationId: Int): List<LocationEntity>

    @Query("SELECT * FROM Location")
    suspend fun getLocations(): List<LocationEntity>

    @Query("SELECT * FROM Location WHERE name = :name COLLATE NOCASE")
    suspend fun getLocationByName(name: String): LocationEntity?

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
    fun getLocationWithAislesWithProducts(locationId: Int): Flow<LocationWithAislesWithProducts?>

    @Transaction
    @Query("SELECT * FROM Location WHERE id in (:locationId)")
    suspend fun getLocationsWithAislesWithProducts(vararg locationId: Int): List<LocationWithAislesWithProducts>

    @Transaction
    @Query("SELECT * FROM Location")
    suspend fun getLocationsWithAislesWithProducts(): List<LocationWithAislesWithProducts>

    /**
     * Shop Specific Queries
     */
    @Query("SELECT * FROM Location WHERE type = 'SHOP'")
    fun getShops(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM Location WHERE type = 'SHOP' AND pinned = 1")
    fun getPinnedShops(): Flow<List<LocationEntity>>

    /**
     * Home Specific Queries
     */
    @Query("SELECT * FROM Location WHERE type = 'HOME'")
    suspend fun getHome(): LocationEntity
}
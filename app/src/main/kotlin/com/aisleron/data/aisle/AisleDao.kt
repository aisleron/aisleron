package com.aisleron.data.aisle

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

@Dao
interface AisleDao : BaseDao<AisleEntity> {
    /**
     * Aisle
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    suspend fun getAisle(aisleId: Int): AisleEntity?

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    suspend fun getAisles(vararg aisleId: Int): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle")
    suspend fun getAisles(): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE locationId = :locationId")
    suspend fun getAislesForLocation(locationId: Int): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE isDefault = 1")
    suspend fun getDefaultAisles(): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle WHERE isDefault = 1 AND locationId = :locationId")
    suspend fun getDefaultAisleFor(locationId: Int): AisleEntity?

    /**
     * Aisle With Location
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    suspend fun getAisleWithLocation(aisleId: Int): AisleWithLocation

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    suspend fun getAislesWithLocation(vararg aisleId: Int): List<AisleWithLocation>

    @Transaction
    @Query("SELECT * FROM Aisle")
    suspend fun getAislesWithLocation(): List<AisleWithLocation>

    /**
     * Aisle With Product
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    suspend fun getAisleWithProducts(aisleId: Int): AisleWithProducts

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    suspend fun getAislesWithProducts(vararg aisleId: Int): List<AisleWithProducts>

    @Transaction
    @Query("SELECT * FROM Aisle")
    suspend fun getAislesWithProducts(): List<AisleWithProducts>

    @Transaction
    suspend fun updateRank(aisle: AisleEntity) {
        moveRanks(aisle.locationId, aisle.rank)
        upsert(aisle)
    }

    @Query("UPDATE Aisle SET rank = rank + 1 WHERE locationId = :locationId and rank >= :fromRank")
    suspend fun moveRanks(locationId: Int, fromRank: Int)
}
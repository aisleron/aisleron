package com.aisleron.data.aisle

import androidx.room.Query
import androidx.room.Transaction
import com.aisleron.data.base.BaseDao

interface AisleDao : BaseDao<AisleEntity> {
    /**
     * Aisle
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    fun getAisle(aisleId: Int): AisleEntity

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    fun getAisles(vararg aisleId: Int): List<AisleEntity>

    @Transaction
    @Query("SELECT * FROM Aisle")
    fun getAisles(): List<AisleEntity>

    /**
     * Aisle With Location
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    fun getAisleWithLocation(aisleId: Int): AisleWithLocation

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    fun getAislesWithLocation(vararg aisleId: Int): List<AisleWithLocation>

    @Transaction
    @Query("SELECT * FROM Aisle")
    fun getAislesWithLocation(): List<AisleWithLocation>

    /**
     * Aisle With Product
     */
    @Transaction
    @Query("SELECT * FROM Aisle WHERE id = :aisleId")
    fun getAisleWithProducts(aisleId: Int): AisleWithProducts

    @Transaction
    @Query("SELECT * FROM Aisle WHERE id in (:aisleId)")
    fun getAislesWithProducts(vararg aisleId: Int): List<AisleWithProducts>

    @Transaction
    @Query("SELECT * FROM Aisle")
    fun getAislesWithProducts(): List<AisleWithProducts>
}
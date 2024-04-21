package com.aisleron.domain.product

import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.base.BaseRepository

interface ProductRepository : BaseRepository<Product> {
    suspend fun getInStock(): List<Product>
    suspend fun getNeeded(): List<Product>
    suspend fun getByFilter(filter: FilterType): List<Product>
    suspend fun getByAisle(aisle: Aisle): List<Product>
    suspend fun getByAisle(aisleId: Int): List<Product>
}
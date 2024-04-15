package com.aisleron.domain.product

import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.FilterType

interface ProductRepository : BaseRepository<Product> {
    suspend fun getInStock(): List<Product>
    suspend fun getNeeded(): List<Product>
    suspend fun getByFilter(filter: FilterType): List<Product>
    suspend fun getByAisle(aisle: Aisle): List<Product>
    suspend fun getByAisle(aisleId: Int): List<Product>
    suspend fun updateStatus(id: Int, inStock: Boolean)
}
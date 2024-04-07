package com.aisleron.domain.product

import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.FilterType

interface ProductRepository : BaseRepository<Product> {
    fun getInStock(): List<Product>
    fun getNeeded(): List<Product>
    fun getByFilter(filter: FilterType): List<Product>
    fun getByAisle(aisle: Aisle): List<Product>
    fun getByAisle(aisleId: Long): List<Product>
}
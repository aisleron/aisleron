package com.aisleron.domain.repositories

import com.aisleron.domain.model.Aisle
import com.aisleron.domain.model.FilterType
import com.aisleron.domain.model.Product

interface ProductRepository : BaseRepository<Product> {
    fun getInStock(): List<Product>
    fun getNeeded(): List<Product>
    fun getByFilter(filter: FilterType) : List<Product>
    fun getByAisle(aisle: Aisle) : List<Product>
    fun getByAisle(aisleId: Long) : List<Product>
}
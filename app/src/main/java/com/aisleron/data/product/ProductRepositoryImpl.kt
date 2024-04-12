package com.aisleron.data.product

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class ProductRepositoryImpl(
    private val db: AisleronDatabase,
    private val productMapper: ProductMapper
) : ProductRepository {
    override suspend fun getInStock(): List<Product> {
        return productMapper.toModelList(db.productDao().getInStockProducts())
    }

    override suspend fun getNeeded(): List<Product> {
        return productMapper.toModelList(db.productDao().getNeededProducts())
    }

    override suspend fun getByFilter(filter: FilterType): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getByAisle(aisle: Aisle): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getByAisle(aisleId: Long): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun get(id: Int): Product? {
        return db.productDao().getProduct(id)?.let { productMapper.toModel(it) }
    }

    override suspend fun getAll(): List<Product> {
        return productMapper.toModelList(db.productDao().getProducts())
    }

    override suspend fun add(item: Product): Int {
        return db.productDao().upsert(productMapper.fromModel(item))[0].toInt()
    }

    override suspend fun update(item: Product) {
        db.productDao().upsert(productMapper.fromModel(item))
    }

    override suspend fun remove(item: Product) {
        db.productDao().delete(productMapper.fromModel(item))
    }

    override suspend fun remove(id: Int) {
        TODO("Not yet implemented")
    }
}
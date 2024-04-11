package com.aisleron.data.product

import com.aisleron.data.AisleronDatabase
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class ProductRepositoryImpl(private val db: AisleronDatabase) : ProductRepository {
    override suspend fun getInStock(): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun getNeeded(): List<Product> {
        TODO("Not yet implemented")
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
        return db.productDao().getProduct(id)?.toProduct()
    }

    override suspend fun getAll(): List<Product> {
        TODO("Not yet implemented")
    }

    override suspend fun add(item: Product): Int {
        return db.productDao().upsert(item.toProductEntity())[0].toInt()
    }

    override suspend fun update(item: Product) {
        db.productDao().upsert(item.toProductEntity())
    }

    override suspend fun remove(item: Product) {
        TODO("Not yet implemented")
    }

    override suspend fun remove(id: Int) {
        TODO("Not yet implemented")
    }
}

private fun ProductEntity.toProduct() = Product(
    id = this.id,
    name = this.name,
    inStock = this.inStock
)

private fun Product.toProductEntity() = ProductEntity(
    id = this.id,
    name = this.name,
    inStock = this.inStock
)
package com.aisleron.testdata.data.product

import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductEntity

class ProductDaoTestImpl : ProductDao {

    private val productList = mutableListOf<ProductEntity>()

    override suspend fun upsert(vararg entity: ProductEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val id: Int
            val existingEntity = getProduct(it.id)
            if (existingEntity == null) {
                id = (productList.maxOfOrNull { e -> e.id }?.toInt() ?: 0) + 1
            } else {
                id = existingEntity.id
                productList.removeAt(productList.indexOf(existingEntity))
            }

            val newEntity = ProductEntity(
                id = id,
                name = it.name,
                inStock = it.inStock
            )

            productList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: ProductEntity) {
        productList.removeIf { it in entity }
    }

    override suspend fun getProduct(productId: Int): ProductEntity? {
        return productList.find { it.id == productId }
    }

    override suspend fun getProducts(vararg productId: Int): List<ProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getProducts(): List<ProductEntity> {
        return productList
    }

    override suspend fun getProductsForAisle(aisleId: Int): List<ProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getInStockProducts(): List<ProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getNeededProducts(): List<ProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getProductByName(name: String): ProductEntity? {
        return productList.find { it.name.uppercase() == name.uppercase() }
    }
}
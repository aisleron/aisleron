package com.aisleron.data.product

class ProductDaoTestImpl : ProductDao {

    private val productList = mutableListOf<ProductEntity>()

    override suspend fun upsert(vararg entity: ProductEntity): List<Long> {
        val result = mutableListOf<Long>()
        entity.forEach {
            val newEntity = ProductEntity(
                id = (productList.maxOfOrNull { e -> e.id }?.toInt() ?: 0) + 1,
                name = it.name,
                inStock = it.inStock
            )
            productList.add(newEntity)
            result.add(newEntity.id.toLong())
        }
        return result
    }

    override suspend fun delete(vararg entity: ProductEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun getProduct(productId: Int): ProductEntity? {
        return productList.find { it.id == productId }
    }

    override suspend fun getProducts(vararg productId: Int): List<ProductEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getProducts(): List<ProductEntity> {
        TODO("Not yet implemented")
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
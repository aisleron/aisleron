package com.aisleron.data.product

class ProductDaoImpl : ProductDao {
    override suspend fun upsert(vararg entity: ProductEntity): List<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(vararg entity: ProductEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun getProduct(productId: Int): ProductEntity? {
        TODO("Not yet implemented")
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
}
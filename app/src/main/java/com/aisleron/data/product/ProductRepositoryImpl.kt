package com.aisleron.data.product

import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.domain.FilterType
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val aisleProductDao: AisleProductDao,
    private val productMapper: ProductMapper
) : ProductRepository {
    override suspend fun getInStock(): List<Product> {
        return productMapper.toModelList(productDao.getInStockProducts())
    }

    override suspend fun getNeeded(): List<Product> {
        return productMapper.toModelList(productDao.getNeededProducts())
    }

    override suspend fun getByFilter(filter: FilterType): List<Product> {
        return when (filter) {
            FilterType.IN_STOCK -> getInStock()
            FilterType.NEEDED -> getNeeded()
            FilterType.ALL -> getAll()
        }
    }

    override suspend fun getByAisle(aisle: Aisle): List<Product> {
        return getByAisle(aisle.id)
    }

    override suspend fun getByAisle(aisleId: Int): List<Product> {
        return productMapper.toModelList(productDao.getProductsForAisle(aisleId))
    }

    override suspend fun get(id: Int): Product? {
        return productDao.getProduct(id)?.let { productMapper.toModel(it) }
    }

    override suspend fun getMultiple(vararg id: Int): List<Product> {
        // '*' is a spread operator required to pass vararg down
        return productMapper.toModelList(productDao.getProducts(*id))
    }

    override suspend fun getAll(): List<Product> {
        return productMapper.toModelList(productDao.getProducts())
    }

    override suspend fun add(item: Product): Int {
        return productDao.upsert(productMapper.fromModel(item))[0].toInt()
    }

    override suspend fun add(items: List<Product>): List<Int> {
        return upsertProducts(items)
    }

    override suspend fun update(item: Product) {
        productDao.upsert(productMapper.fromModel(item))
    }

    override suspend fun update(items: List<Product>) {
        upsertProducts(items)
    }

    private suspend fun upsertProducts(products: List<Product>): List<Int> {
        return productDao
            .upsert(*productMapper.fromModelList(products).map { it }.toTypedArray())
            .map { it.toInt() }
    }

    override suspend fun remove(item: Product) {
        productDao.remove(productMapper.fromModel(item), aisleProductDao)

    }
}
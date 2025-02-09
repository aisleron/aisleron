package com.aisleron.data.product

import com.aisleron.data.DbInitializer
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisle.AisleMapper
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.databaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.product.Product
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.module.Module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductRepositoryImplTest : KoinTest {
    private lateinit var productRepositoryImpl: ProductRepositoryImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = getKoinModules()
    )

    private fun getKoinModules(): List<Module> = listOf(
        daoModule, databaseTestModule, repositoryModule, useCaseModule
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        DbInitializer(
            get<LocationDao>(), get<AisleDao>(), TestScope(UnconfinedTestDispatcher())
        ).invoke()

        val createSampleDataUseCase = get<CreateSampleDataUseCase>()
        runBlocking {
            createSampleDataUseCase()
        }

        productRepositoryImpl = ProductRepositoryImpl(
            productDao = get<ProductDao>(),
            aisleProductDao = get<AisleProductDao>(),
            productMapper = ProductMapper()
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getInStock_ReturnsInStockProducts() {
        val inStockCount = runBlocking { get<ProductDao>().getProducts().count { it.inStock } }

        val inStockProducts = runBlocking { productRepositoryImpl.getInStock() }

        assertTrue(inStockProducts.isNotEmpty())
        assertEquals(inStockCount, inStockProducts.count())
    }

    @Test
    fun getNeeded_ReturnsNeededProducts() = runTest {
        val neededCount = get<ProductDao>().getProducts().count { !it.inStock }

        val neededProducts = productRepositoryImpl.getNeeded()

        assertTrue(neededProducts.isNotEmpty())
        assertEquals(neededCount, neededProducts.count())
    }

    @Test
    fun getByFilter_RequestInStock_ReturnsInStockProducts() = runTest {
        val inStockCount = get<ProductDao>().getProducts().count { it.inStock }

        val inStockProducts = productRepositoryImpl.getByFilter(FilterType.IN_STOCK)

        assertTrue(inStockProducts.isNotEmpty())
        assertEquals(inStockCount, inStockProducts.count())
    }

    @Test
    fun getByFilter_RequestInNeeded_ReturnsNeededProducts() = runTest {
        val neededCount = get<ProductDao>().getProducts().count { !it.inStock }

        val neededProducts = productRepositoryImpl.getByFilter(FilterType.NEEDED)

        assertTrue(neededProducts.isNotEmpty())
        assertEquals(neededCount, neededProducts.count())
    }

    @Test
    fun getByFilter_RequestAll_ReturnsAllProducts() = runTest {
        val allCount = get<ProductDao>().getProducts().count()

        val allProducts = productRepositoryImpl.getByFilter(FilterType.ALL)

        assertTrue(allProducts.isNotEmpty())
        assertEquals(allCount, allProducts.count())
    }

    @Test
    fun getByAisle_WithAisleId_ReturnAisleProducts() = runTest {
        val aisleId = get<AisleProductDao>().getAisleProducts().first().aisleProduct.aisleId
        val aisleProductCount =
            get<AisleProductDao>().getAisleProducts().count { it.aisleProduct.aisleId == aisleId }

        val products = productRepositoryImpl.getByAisle(aisleId)

        assertEquals(aisleProductCount, products.count())
    }

    @Test
    fun getByAisle_WithAisleEntity_ReturnAisleProducts() = runTest {
        val aisleId = get<AisleProductDao>().getAisleProducts().first().aisleProduct.aisleId
        val aisle = AisleMapper().toModel(get<AisleDao>().getAisle(aisleId)!!)

        val aisleProductCount =
            get<AisleProductDao>().getAisleProducts().count { it.aisleProduct.aisleId == aisle.id }


        val products = productRepositoryImpl.getByAisle(aisle)

        assertEquals(aisleProductCount, products.count())
    }

    @Test
    fun getByName_ValidNameProvided_ReturnProduct() = runTest {
        val productName = get<ProductDao>().getProducts().first().name

        val product = productRepositoryImpl.getByName(productName)

        assertNotNull(product)
    }

    @Test
    fun getByName_InvalidNameProvided_ReturnNull() = runTest {
        val productName = "Not a product that exists in the database"

        val product = productRepositoryImpl.getByName(productName)

        assertNull(product)
    }

    @Test
    fun get_ValidIdProvided_ReturnProduct() = runTest {
        val productId = get<ProductDao>().getProducts().first().id

        val product = productRepositoryImpl.get(productId)

        assertNotNull(product)
    }

    @Test
    fun get_InvalidIdProvided_ReturnProduct() = runTest {
        val productId = -10001

        val product = productRepositoryImpl.get(productId)

        assertNull(product)
    }

    @Test
    fun getMultiple_MultipleValidIdsProvided_CorrectProductsReturned() = runTest {
        val allProducts = get<ProductDao>().getProducts()
        val productIdOne = allProducts.first().id
        val productIdTwo = allProducts.last().id

        val products = productRepositoryImpl.getMultiple(productIdOne, productIdTwo)

        assertEquals(2, products.count())
        assertNotNull(products.firstOrNull { it.id == productIdOne })
        assertNotNull(products.firstOrNull { it.id == productIdTwo })
    }

    @Test
    fun getMultiple_InvalidIdsProvided_OnlyCorrectProductsReturned() = runTest {
        val allProducts = get<ProductDao>().getProducts()
        val productIdOne = allProducts.first().id
        val productIdTwo = -10001

        val products = productRepositoryImpl.getMultiple(productIdOne, productIdTwo)

        assertEquals(1, products.count())
        assertNotNull(products.firstOrNull { it.id == productIdOne })
        assertNull(products.firstOrNull { it.id == productIdTwo })
    }

    @Test
    fun getAll_AllProductsReturned() = runTest {
        val allCount = get<ProductDao>().getProducts().count()

        val allProducts = productRepositoryImpl.getAll()

        assertTrue(allProducts.isNotEmpty())
        assertEquals(allCount, allProducts.count())
    }

    @Test
    fun add_SingleProductProvided_ProductAdded() = runTest {
        val productDao = get<ProductDao>()
        val product = Product(
            id = 0,
            name = "Product Repository Add Product Test",
            inStock = true
        )

        val productCountBefore = productDao.getProducts().firstOrNull { it.name == product.name }
        val newProductId = productRepositoryImpl.add(product)
        val productAfter = productDao.getProducts().firstOrNull { it.name == product.name }

        assertNull(productCountBefore)
        assertNotNull(productAfter)
        assertEquals(newProductId, productAfter.id)
    }

    @Test
    fun add_MultipleProductsProvided_ProductsAdded() = runTest {
        val productDao = get<ProductDao>()
        val productCountBefore = productDao.getProducts().count()

        val newProducts = listOf(
            Product(
                id = 0,
                name = "Product Repository Multi Add Test Product One",
                inStock = true
            ),
            Product(
                id = 0,
                name = "Product Repository Multi Add Test Product Two",
                inStock = false
            )
        )

        val newProductIds = productRepositoryImpl.add(newProducts)

        val productCountAfter = productDao.getProducts().count()
        val newProductOne = productDao.getProduct(newProductIds.first())
        val newProductTwo = productDao.getProduct(newProductIds.last())

        assertEquals(productCountBefore + 2, productCountAfter)
        assertEquals(2, newProductIds.count())
        assertNotNull(newProductOne)
        assertNotNull(newProductTwo)
    }

    @Test
    fun update_SingleProductProvided_ProductUpdated() = runTest {
        val productDao = get<ProductDao>()
        val productBefore = productDao.getProducts().first()
        val product = Product(
            id = productBefore.id,
            name = "${productBefore.name} Updated",
            inStock = productBefore.inStock
        )

        val productCountBefore = productDao.getProducts().count()
        productRepositoryImpl.update(product)
        val productCountAfter = productDao.getProducts().count()

        val productAfter = productDao.getProduct(productBefore.id)

        assertEquals(productCountBefore, productCountAfter)
        assertEquals(product.name, productAfter?.name)
    }

    @Test
    fun update_MultipleProductsProvided_ProductsUpdated() = runTest {
        val productDao = get<ProductDao>()
        val productCountBefore = productDao.getProducts().count()
        val productOneBefore = productDao.getProducts().first()
        val productTwoBefore = productDao.getProducts().last()

        val products = listOf(
            Product(
                id = productOneBefore.id,
                name = "${productOneBefore.name} Updated",
                inStock = productOneBefore.inStock
            ),
            Product(
                id = productTwoBefore.id,
                name = "${productTwoBefore.name} Updated",
                inStock = productTwoBefore.inStock
            )
        )

        productRepositoryImpl.update(products)

        val productCountAfter = productDao.getProducts().count()
        val productOneAfter = productDao.getProduct(productOneBefore.id)
        val productTwoAfter = productDao.getProduct(productTwoBefore.id)

        assertEquals(productCountBefore, productCountAfter)
        assertEquals(products.first { it.id == productOneBefore.id }.name, productOneAfter?.name)
        assertEquals(products.first { it.id == productTwoBefore.id }.name, productTwoAfter?.name)
    }

    @Test
    fun remove_ValidProductProvided_ProductRemoved() = runTest {
        val productDao = get<ProductDao>()
        val productBefore = productDao.getProducts().first()
        val product = Product(
            id = productBefore.id,
            name = productBefore.name,
            inStock = productBefore.inStock
        )

        val productCountBefore = productDao.getProducts().count()
        productRepositoryImpl.remove(product)
        val productCountAfter = productDao.getProducts().count()
        val productAfter = productDao.getProduct(productBefore.id)

        assertEquals(productCountBefore - 1, productCountAfter)
        assertNull(productAfter)
    }

    @Test
    fun remove_InvalidProductProvided_NoProductsRemoved() = runTest {
        val productDao = get<ProductDao>()
        val product = Product(
            id = -10001,
            name = "Test remove_InvalidProductProvided_NoProductsRemoved",
            inStock = false
        )

        val productCountBefore = productDao.getProducts().count()
        productRepositoryImpl.remove(product)
        val productCountAfter = productDao.getProducts().count()

        assertEquals(productCountBefore, productCountAfter)
    }

    @Test
    fun remove_ValidProductProvided_AisleProductEntriesRemoved() = runTest {
        val productBefore = get<ProductDao>().getProducts().first()
        val product = Product(
            id = productBefore.id,
            name = productBefore.name,
            inStock = productBefore.inStock
        )

        val aisleProductDao = get<AisleProductDao>()
        val aisleProductCountBefore = aisleProductDao.getAisleProducts().count()
        val aisleProductCountProduct =
            aisleProductDao.getAisleProducts().count { it.product.id == productBefore.id }

        productRepositoryImpl.remove(product)

        val aisleProductCountAfter = aisleProductDao.getAisleProducts().count()

        assertEquals(aisleProductCountBefore - aisleProductCountProduct, aisleProductCountAfter)
    }
}
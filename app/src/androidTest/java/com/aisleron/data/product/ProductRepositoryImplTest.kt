/*
 * Copyright (C) 2025 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.data.product

import com.aisleron.data.DbInitializer
import com.aisleron.data.aisle.AisleDao
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.data.location.LocationDao
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoModule
import com.aisleron.di.inMemoryDatabaseTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.domain.product.Product
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
        daoModule, inMemoryDatabaseTestModule, repositoryModule, useCaseModule
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        DbInitializer(
            get<LocationDao>(), get<AisleDao>(), TestScope(UnconfinedTestDispatcher())
        ).invoke()

        runBlocking {
            get<CreateSampleDataUseCase>().invoke()
        }

        productRepositoryImpl = ProductRepositoryImpl(
            productDao = get<ProductDao>(),
            aisleProductDao = get<AisleProductDao>(),
            productMapper = ProductMapper()
        )
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
            inStock = true,
            qtyNeeded = 0
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
                inStock = true,
                qtyNeeded = 0
            ),
            Product(
                id = 0,
                name = "Product Repository Multi Add Test Product Two",
                inStock = false,
                qtyNeeded = 0
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
            inStock = productBefore.inStock,
            qtyNeeded = productBefore.qtyNeeded
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
                inStock = productOneBefore.inStock,
                qtyNeeded = productOneBefore.qtyNeeded
            ),
            Product(
                id = productTwoBefore.id,
                name = "${productTwoBefore.name} Updated",
                inStock = productTwoBefore.inStock,
                qtyNeeded = productTwoBefore.qtyNeeded
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
            inStock = productBefore.inStock,
            qtyNeeded = productBefore.qtyNeeded
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
            inStock = false,
            qtyNeeded = 0
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
            inStock = productBefore.inStock,
            qtyNeeded = 0
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
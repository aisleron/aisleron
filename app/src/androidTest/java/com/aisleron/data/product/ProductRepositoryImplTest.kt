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

import com.aisleron.data.RepositoryImplTest
import com.aisleron.data.aisleproduct.AisleProductDao
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.preferences.TrackingMode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductRepositoryImplTest : RepositoryImplTest<Product>() {
    val productRepository: ProductRepository get() = repository as ProductRepository

    override fun initRepository(): BaseRepository<Product> =
        ProductRepositoryImpl(
            productDao = get<ProductDao>(),
            aisleProductDao = get<AisleProductDao>(),
            productMapper = ProductMapper()
        )

    override suspend fun getSingleNewItem(): Product =
        Product(
            id = 0,
            name = "Product Repository Add Product Test",
            inStock = true,
            qtyNeeded = 0.0,
            noteId = null,
            qtyIncrement = 1.0,
            trackingMode = TrackingMode.DEFAULT,
            unitOfMeasure = "Qty"
        )

    override suspend fun getMultipleNewItems(): List<Product> =
        listOf(
            getSingleNewItem(),
            getSingleNewItem().copy(name = "Product Repository Add Product Test 2")
        )

    override suspend fun getInvalidItem(): Product =
        getSingleNewItem().copy(id = -1)

    override fun getUpdatedItem(item: Product): Product =
        item.copy(name = "${item.name} Updated")

    @Test
    fun getByName_ValidNameProvided_ReturnProduct() = runTest {
        val productName = get<ProductDao>().getProducts().first().name

        val product = productRepository.getByName(productName)

        assertNotNull(product)
    }

    @Test
    fun getByName_InvalidNameProvided_ReturnNull() = runTest {
        val productName = "Not a product that exists in the database"

        val product = productRepository.getByName(productName)

        assertNull(product)
    }

    @Test
    fun remove_ValidProductProvided_AisleProductEntriesRemoved() = runTest {
        val product = productRepository.getAll().first()


        val aisleProductDao = get<AisleProductDao>()
        val aisleProductCountBefore = aisleProductDao.getAisleProducts().count()
        val aisleProductCountProduct =
            aisleProductDao.getAisleProducts().count { it.product.id == product.id }

        productRepository.remove(product)

        val aisleProductCountAfter = aisleProductDao.getAisleProducts().count()

        assertEquals(aisleProductCountBefore - aisleProductCountProduct, aisleProductCountAfter)
    }
}
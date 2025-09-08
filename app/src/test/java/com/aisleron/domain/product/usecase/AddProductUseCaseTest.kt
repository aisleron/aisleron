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

package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.GetAisleMaxRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddProductUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var addProductUseCase: AddProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()

        addProductUseCase = AddProductUseCaseImpl(
            productRepository,
            GetDefaultAislesUseCase(testData.getRepository<AisleRepository>()),
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>()),
            IsProductNameUniqueUseCase(testData.getRepository<ProductRepository>()),
            GetAisleMaxRankUseCase(testData.getRepository<AisleProductRepository>())
        )

        existingProduct = runBlocking {
            productRepository.getAll()[1]
        }
    }

    @Test
    fun addProduct_IsDuplicateName_ThrowsException() {
        runBlocking {
            val newProduct = testData.getRepository<ProductRepository>().getAll()[1].copy(id = 0)
            assertThrows<AisleronException.DuplicateProductNameException> {
                addProductUseCase(newProduct, null)
            }
        }
    }

    @Test
    fun addProduct_IsExistingProduct_ThrowsException() {
        val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )

        runBlocking {
            assertThrows<AisleronException.DuplicateProductException> {
                addProductUseCase(updateProduct, null)
            }
        }
    }

    private fun getNewProduct(): Product {
        return Product(
            id = 0,
            name = "New Product 1",
            inStock = false,
            qtyNeeded = 0
        )
    }

    @Test
    fun addProduct_IsNewProduct_ProductCreated() {
        val newProduct = getNewProduct()
        val countBefore: Int
        val countAfter: Int
        val insertedProduct: Product?
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            val id = addProductUseCase(newProduct, null)
            insertedProduct = productRepository.get(id)
            countAfter = productRepository.getAll().count()
        }
        Assertions.assertNotNull(insertedProduct)
        Assertions.assertEquals(countBefore + 1, countAfter)
        Assertions.assertEquals(newProduct.name, insertedProduct?.name)
        Assertions.assertEquals(newProduct.inStock, insertedProduct?.inStock)
    }

    @Test
    fun addProduct_ProductInserted_AddsAisleProducts() {
        val newProduct = getNewProduct()
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val locationCount: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            locationCount = testData.getRepository<LocationRepository>().getAll().count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            addProductUseCase(newProduct, null)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        Assertions.assertEquals(aisleProductCountBefore + locationCount, aisleProductCountAfter)
    }

    @Test
    fun addProduct_AisleProvided_ProductAddedToAisle() = runTest {
        val newProduct = getNewProduct()
        val aisle = testData.getRepository<AisleRepository>().getAll().first { !it.isDefault }
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        val aisleProductCountBefore =
            aisleProductRepository.getAll().count { it.aisleId == aisle.id }

        val newProductId = addProductUseCase(newProduct, aisle)

        val aisleProductCountAfter =
            aisleProductRepository.getAll().count { it.aisleId == aisle.id }

        val aisleProduct = aisleProductRepository.getAll()
            .firstOrNull { it.aisleId == aisle.id && it.product.id == newProductId }

        Assertions.assertEquals(aisleProductCountBefore + 1, aisleProductCountAfter)
        Assertions.assertNotNull(aisleProduct)
    }

    /**
     * Add Product when aisle has no existing products, i.e. max rank is null
     * No aisle provided > Not in default count is 0
     */
}
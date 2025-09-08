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
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateProductUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var updateProductUseCase: UpdateProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()
        existingProduct = runBlocking { productRepository.get(1)!! }
        updateProductUseCase = UpdateProductUseCase(
            productRepository, IsProductNameUniqueUseCase(productRepository)
        )
    }

    @Test
    fun updateProduct_IsDuplicateName_ThrowsException() {
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            val id = productRepository.add(
                Product(
                    id = 2,
                    name = "Product 2",
                    inStock = false,
                    qtyNeeded = 0
                )
            )

            val updateProduct = productRepository.get(id)!!.copy(name = existingProduct.name)
            assertThrows<AisleronException.DuplicateProductNameException> {
                updateProductUseCase(updateProduct)
            }
        }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() {
        val updateProduct =
            existingProduct.copy(
                name = existingProduct.name + " Updated",
                inStock = !existingProduct.inStock
            )
        val updatedProduct: Product?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            updateProductUseCase(updateProduct)
            updatedProduct = productRepository.getByName(updateProduct.name)
            countAfter = productRepository.getAll().count()
        }
        assertNotNull(updatedProduct)
        assertEquals(countBefore, countAfter)
        assertEquals(updateProduct.id, updatedProduct?.id)
        assertEquals(updateProduct.name, updatedProduct?.name)
        assertEquals(updateProduct.inStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() {
        val newProduct = existingProduct.copy(
            id = 1030535,
            name = existingProduct.name + " Inserted"
        )
        val updatedProduct: Product?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            updateProductUseCase(newProduct)
            updatedProduct = productRepository.getByName(newProduct.name)
            countAfter = productRepository.getAll().count()
        }
        assertNotNull(updatedProduct)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newProduct.name, updatedProduct?.name)
        assertEquals(newProduct.inStock, updatedProduct?.inStock)
    }
}
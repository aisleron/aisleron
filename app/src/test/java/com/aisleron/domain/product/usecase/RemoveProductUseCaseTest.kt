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
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RemoveProductUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var removeProductUseCase: RemoveProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()

        removeProductUseCase = RemoveProductUseCase(productRepository)

        existingProduct = runBlocking { productRepository.get(1)!! }
    }

    @Test
    fun removeProduct_IsExistingProduct_ProductRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedProduct: Product?
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            removedProduct = productRepository.get(existingProduct.id)
            countAfter = productRepository.getAll().count()
        }
        assertNull(removedProduct)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeProduct_IsNonExistentProduct_NoProductsRemoved() {
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            val productRepository = testData.getRepository<ProductRepository>()
            countBefore = productRepository.getAll().count()
            removeProductUseCase(productRepository.getAll().maxOf { it.id } + 1000)
            countAfter = productRepository.getAll().count()
        }
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun removeProduct_ProductRemoved_AisleProductsRemoved() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCountProduct: Int
        runBlocking {
            val aisleProductRepository = testData.getRepository<AisleProductRepository>()
            val aisleProductList = aisleProductRepository.getAll()
                .filter { it.product.id == existingProduct.id }
            aisleProductCountProduct = aisleProductList.count()
            aisleProductCountBefore = aisleProductRepository.getAll().count()
            removeProductUseCase(existingProduct.id)
            aisleProductCountAfter = aisleProductRepository.getAll().count()
        }
        assertEquals(
            aisleProductCountBefore - aisleProductCountProduct,
            aisleProductCountAfter
        )
    }
}
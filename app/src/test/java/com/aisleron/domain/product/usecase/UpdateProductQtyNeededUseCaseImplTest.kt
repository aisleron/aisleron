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
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateProductQtyNeededUseCaseImplTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateProductQtyNeededUseCase: UpdateProductQtyNeededUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()
        updateProductQtyNeededUseCase = UpdateProductQtyNeededUseCaseImpl(
            GetProductUseCase(productRepository),
            UpdateProductUseCase(productRepository, IsProductNameUniqueUseCase(productRepository))
        )
    }

    private suspend fun getProduct(initialQty: Int): Product {
        val product = Product(
            id = 0,
            name = "qtyNeeded Test Product",
            inStock = false,
            qtyNeeded = initialQty
        )

        val id = testData.getRepository<ProductRepository>().add(product)
        return product.copy(id = id)
    }

    @Test
    fun updateProductQtyNeeded_QtyIncremented_QtyNeededUpdated() = runTest {
        val productBefore = getProduct(5)
        val newQty = productBefore.qtyNeeded + 3

        updateProductQtyNeededUseCase(productBefore.id, newQty)

        val productAfter = testData.getRepository<ProductRepository>().get(productBefore.id)

        assertNotNull(productAfter)
        assertEquals(newQty, productAfter?.qtyNeeded)
    }

    @Test
    fun updateProductQtyNeeded_QtyDecremented_QtyNeededUpdated() = runTest {
        val productBefore = getProduct(5)
        val newQty = productBefore.qtyNeeded - 3

        val productAfter = updateProductQtyNeededUseCase(productBefore.id, newQty)

        assertNotNull(productAfter)
        assertEquals(newQty, productAfter?.qtyNeeded)
    }

    @Test
    fun updateProductQtyNeeded_ProductDoesNotExist_ReturnNull() = runTest {
        val updatedProduct = updateProductQtyNeededUseCase(1001, 5)
        assertNull(updatedProduct)
    }

    @Test
    fun updateProductQtyNeeded_IsNegativeNumber_ThrowsException() = runTest {
        val productBefore = getProduct(0)
        val newQty = -1

        assertThrows<IllegalArgumentException> {
            updateProductQtyNeededUseCase(productBefore.id, newQty)
        }
    }
}
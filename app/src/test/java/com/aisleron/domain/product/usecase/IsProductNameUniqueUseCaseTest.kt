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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsProductNameUniqueUseCaseTest {

    private lateinit var isProductNameUniqueUseCase: IsProductNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isProductNameUniqueUseCase =
            IsProductNameUniqueUseCase(testData.getRepository<ProductRepository>())
    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() = runTest {
        val newProduct = existingProduct.copy(id = 0, name = "Product Unique Name")

        val result = isProductNameUniqueUseCase(newProduct)

        Assertions.assertNotEquals(existingProduct.name, newProduct.name)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_ProductIdsMatch_ReturnTrue() = runTest {
        val newProduct = existingProduct.copy(inStock = true)

        val result = isProductNameUniqueUseCase(newProduct)

        Assertions.assertEquals(existingProduct.id, newProduct.id)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchIdsDiffer_ReturnFalse() = runTest {
        val newProduct = existingProduct.copy(id = 0)

        val result = isProductNameUniqueUseCase(newProduct)

        Assertions.assertEquals(existingProduct.name, newProduct.name)
        Assertions.assertNotEquals(existingProduct.id, newProduct.id)
        Assertions.assertFalse(result)
    }

    companion object {

        private lateinit var testData: TestDataManager
        private lateinit var existingProduct: Product

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            testData = TestDataManager()

            existingProduct = runBlocking {
                testData.getRepository<ProductRepository>().get(1)!!
            }
        }
    }
}
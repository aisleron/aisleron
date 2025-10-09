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

import com.aisleron.di.TestDependencyManager
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class UpdateProductStatusUseCaseTest {
    private lateinit var dm: TestDependencyManager
    private lateinit var updateProductStatusUseCase: UpdateProductStatusUseCase

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        updateProductStatusUseCase = dm.getUseCase()
    }

    @ParameterizedTest(name = "Test when inStock Status is {0}")
    @MethodSource("inStockArguments")
    fun updateProductStatus_ProductExists_StatusUpdated(inStock: Boolean) = runTest {
        val existingProduct = dm.getRepository<ProductRepository>().getAll().first()

        val updatedProduct = updateProductStatusUseCase(existingProduct.id, inStock)

        assertNotNull(updatedProduct)
        assertEquals(existingProduct.id, updatedProduct?.id)
        assertEquals(existingProduct.name, updatedProduct?.name)
        assertEquals(inStock, updatedProduct?.inStock)
    }

    @Test
    fun updateProductStatus_ProductDoesNotExist_ReturnNull() = runTest {
        val updatedProduct = updateProductStatusUseCase(1001, true)
        assertNull(updatedProduct)
    }

    private companion object {
        @JvmStatic
        fun inStockArguments(): Stream<Arguments> = Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        )
    }
}
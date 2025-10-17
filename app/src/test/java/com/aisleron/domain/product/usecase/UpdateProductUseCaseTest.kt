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
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateProductUseCaseTest {

    private lateinit var dm: TestDependencyManager
    private lateinit var updateProductUseCase: UpdateProductUseCase
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setUp() {
        dm = TestDependencyManager()
        repository = dm.getRepository<ProductRepository>()
        updateProductUseCase = dm.getUseCase<UpdateProductUseCase>()
    }

    private suspend fun existingProduct(): Product = repository.getAll().first()

    @Test
    fun updateProduct_IsDuplicateName_ThrowsException() = runTest {
        val existingProduct = existingProduct()
        val id = repository.add(
            existingProduct.copy(id = 0, name = "Product 2", inStock = !existingProduct.inStock)
        )

        val updateProduct = repository.get(id)!!.copy(name = existingProduct.name)

        assertThrows<AisleronException.DuplicateProductNameException> {
            updateProductUseCase(updateProduct)
        }
    }

    @Test
    fun updateProduct_ProductExists_RecordUpdated() = runTest {
        val existingProduct = existingProduct()
        val updateProduct = existingProduct.copy(
            name = existingProduct.name + " Updated",
            inStock = !existingProduct.inStock
        )

        val countBefore: Int = repository.getAll().count()

        updateProductUseCase(updateProduct)

        val updatedProduct: Product? = repository.getByName(updateProduct.name)
        assertNotNull(updatedProduct)
        assertEquals(updateProduct.id, updatedProduct?.id)
        assertEquals(updateProduct.name, updatedProduct?.name)
        assertEquals(updateProduct.inStock, updatedProduct?.inStock)

        val countAfter: Int = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun updateProduct_ProductDoesNotExist_RecordCreated() = runTest {
        val existingProduct = existingProduct()
        val newProduct = existingProduct.copy(
            id = 1030535,
            name = existingProduct.name + " Inserted"
        )

        val countBefore: Int = repository.getAll().count()

        updateProductUseCase(newProduct)

        val updatedProduct: Product? = repository.getByName(newProduct.name)
        assertNotNull(updatedProduct)
        assertEquals(newProduct.name, updatedProduct?.name)
        assertEquals(newProduct.inStock, updatedProduct?.inStock)

        val countAfter: Int = repository.getAll().count()
        assertEquals(countBefore + 1, countAfter)
    }
}
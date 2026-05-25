/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.data.aisleproduct

import com.aisleron.data.RepositoryImplTest
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AisleProductRepositoryImplTest : RepositoryImplTest<AisleProduct>() {
    val aisleProductRepository: AisleProductRepository get() = repository as AisleProductRepository

    override fun initRepository(): BaseRepository<AisleProduct> = AisleProductRepositoryImpl(
        aisleProductDao = get<AisleProductDao>(),
        aisleProductRankMapper = AisleProductRankMapper()
    )

    override suspend fun getSingleNewItem(): AisleProduct {
        val product = get<ProductRepository>().getAll().first()

        return AisleProduct(
            id = -1,
            rank = 999,
            aisleId = 101,
            product = product
        )
    }

    override suspend fun getMultipleNewItems(): List<AisleProduct> {
        val product1 = get<ProductRepository>().getAll().first()
        val product2 = get<ProductRepository>().getAll().last()

        return listOf(
            AisleProduct(
                id = 0,
                rank = 998,
                aisleId = 101,
                product = product1
            ),

            AisleProduct(
                id = 0,
                rank = 999,
                aisleId = 101,
                product = product2
            )
        )
    }

    override suspend fun getInvalidItem(): AisleProduct {
        val product = get<ProductRepository>().getAll().first()

        return AisleProduct(
            id = 0,
            rank = 999,
            aisleId = 101,
            product = product
        )
    }

    override fun getUpdatedItem(item: AisleProduct): AisleProduct =
        item.copy(rank = item.rank + 1)

    @Test
    fun add_SingleAisleProductProvided_AisleProductAdded() = runTest {
        val item = getSingleNewItem()

        val countBefore = repository.getAll().count()

        val newId = repository.add(item)
        val countAfter = repository.getAll().count()
        val newItem = repository.get(newId)

        assertEquals(countBefore + 1, countAfter)
        assertNotNull(newItem)
    }

    @Test
    fun remove_ValidAisleProductProvided_AisleProductRemoved() = runTest {
        val itemBefore = repository.getAll().first()
        val countBefore = repository.getAll().count()

        repository.remove(itemBefore)
        val countAfter = repository.getAll().count()
        val itemAfter = repository.get(itemBefore.id)

        assertEquals(countBefore - 1, countAfter)
        assertNull(itemAfter)
    }

    @Test
    fun remove_InvalidAisleProductProvided_NoAisleProductRemoved() = runTest {
        val countBefore = repository.getAll().count()
        val item = getInvalidItem()

        repository.remove(item)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }

    @Test
    fun getProductAisles_ValidProduct_AislesReturned() = runTest {
        val product = get<ProductRepository>().getAll().first()
        val aisleProducts = aisleProductRepository.getProductAisles(product.id)

        assertTrue(aisleProducts.any())
    }

    @Test
    fun getProductAisles_InvalidProduct_NoAislesReturned() = runTest {
        val aisleProducts = aisleProductRepository.getProductAisles(-100)

        assertFalse(aisleProducts.any())
    }

    @Test
    fun removeProductsFromAisle_ValidAisle_ProductsRemoved() = runTest {
        val countBefore = repository.getAll().count()
        val aisleId = repository.getAll().first().aisleId
        val productCount = repository.getAll().count { it.aisleId == aisleId }

        aisleProductRepository.removeProductsFromAisle(aisleId)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore - productCount, countAfter)
    }

    @Test
    fun removeProductsFromAisle_InvalidAisle_NoProductsRemoved() = runTest {
        val countBefore = repository.getAll().count()

        aisleProductRepository.removeProductsFromAisle(-100)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
    }

    @Test
    fun restoreProductsToAisle_AisleHasRemovedProducts_ProductsRestored() = runTest {
        val countBefore = repository.getAll().count()
        val aisleId = repository.getAll().first().aisleId
        aisleProductRepository.removeProductsFromAisle(aisleId)

        aisleProductRepository.restoreProductsToAisle(aisleId)

        val countAfter = repository.getAll().count()
        assertEquals(countBefore, countAfter)
    }

    @Test
    fun update_SingleAisleProductProvided_AisleProductUpdated() = runTest {
        val dummyRank = 90000
        val countBefore = repository.getAll().count()
        val apBefore = repository.getAll().first()

        repository.update(apBefore.copy(rank = dummyRank))

        val apAfter = repository.get(apBefore.id)
        val countAfter = repository.getAll().count()

        assertEquals(countBefore, countAfter)
        assertEquals(apBefore.copy(rank = dummyRank), apAfter)
    }

    @Test
    fun update_MultipleAisleProductsProvided_AisleProductsUpdated() = runTest {
        val dummyRank = 90000
        val countBefore = repository.getAll().count()
        val oneBefore = repository.getAll().first()
        val twoBefore = repository.getAll().last()

        val aisleProducts = listOf(
            oneBefore.copy(rank = dummyRank),
            twoBefore.copy(rank = dummyRank)
        )

        repository.update(aisleProducts)

        val countAfter = repository.getAll().count()
        val oneAfter = repository.get(oneBefore.id)
        val twoAfter = repository.get(twoBefore.id)

        assertEquals(countBefore, countAfter)
        assertEquals(oneBefore.copy(rank = dummyRank), oneAfter)
        assertEquals(twoBefore.copy(rank = dummyRank), twoAfter)
    }
}
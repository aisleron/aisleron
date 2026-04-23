/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.data.productvariant

import com.aisleron.data.RepositoryImplTest
import com.aisleron.data.product.ProductDao
import com.aisleron.data.product.ProductMapper
import com.aisleron.data.product.ProductRepositoryImpl
import com.aisleron.domain.base.BaseRepository
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.productvariant.ProductVariant
import com.aisleron.domain.productvariant.ProductVariantRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProductVariantRepositoryImplTest : RepositoryImplTest<ProductVariant>() {
    private val productVariantRepository: ProductVariantRepository get() = repository as ProductVariantRepository
    private val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(
            productDao = get<ProductDao>(),
            aisleProductDao = get(),
            productVariantDao = get<ProductVariantDao>(),
            productMapper = ProductMapper()
        )
    }

    override fun initRepository(): BaseRepository<ProductVariant> =
        ProductVariantRepositoryImpl(
            productVariantDao = get<ProductVariantDao>(),
            productVariantMapper = ProductVariantMapper()
        )

    private suspend fun getTestProduct(): Product {
        val product = Product(
            id = 0,
            name = "Test Product for Variant",
            inStock = true,
            qtyNeeded = 1.0,
            noteId = null,
            qtyIncrement = 1.0,
            trackingMode = com.aisleron.domain.preferences.TrackingMode.DEFAULT,
            unitOfMeasure = "Qty"
        )
        val productId = productRepository.add(product)
        return productRepository.get(productId)!!
    }

    override suspend fun getSingleNewItem(): ProductVariant {
        val product = getTestProduct()
        return ProductVariant(
            id = 0,
            productId = product.id,
            barcode = "1234567890123",
            createdAt = System.currentTimeMillis()
        )
    }

    override suspend fun getMultipleNewItems(): List<ProductVariant> {
        val product = getTestProduct()
        return listOf(
            ProductVariant(
                id = 0,
                productId = product.id,
                barcode = "1234567890123",
                createdAt = System.currentTimeMillis()
            ),
            ProductVariant(
                id = 0,
                productId = product.id,
                barcode = "9876543210987",
                createdAt = System.currentTimeMillis() + 1000
            )
        )
    }

    override suspend fun getInvalidItem(): ProductVariant {
        val product = getTestProduct()
        return ProductVariant(
            id = -1,
            productId = product.id,
            barcode = "0000000000000",
            createdAt = System.currentTimeMillis()
        )
    }

    override fun getUpdatedItem(item: ProductVariant): ProductVariant =
        // Use unique barcode based on item id to avoid unique constraint violations
        item.copy(barcode = "9999999${item.id.toString().padStart(6, '0')}")

    @Test
    fun getByBarcode_ValidBarcodeProvided_ReturnVariant() = runTest {
        val variant = getSingleNewItem()
        val variantId = productVariantRepository.add(variant)

        val foundVariant = productVariantRepository.getByBarcode(variant.barcode).first()

        assertNotNull(foundVariant)
        assertEquals(variantId, foundVariant.id)
        assertEquals(variant.barcode, foundVariant.barcode)
    }

    @Test
    fun getByBarcode_InvalidBarcodeProvided_ReturnNull() = runTest {
        val foundVariant = productVariantRepository.getByBarcode("0000000000000").first()

        assertNull(foundVariant)
    }

    @Test
    fun getByProductId_ValidProductIdProvided_ReturnVariants() = runTest {
        val variants = getMultipleNewItems()
        productVariantRepository.add(variants)

        val foundVariants = productVariantRepository.getByProductId(variants.first().productId).first()

        assertEquals(2, foundVariants.count())
    }

    @Test
    fun getByProductId_InvalidProductIdProvided_ReturnEmptyList() = runTest {
        val foundVariants = productVariantRepository.getByProductId(-1).first()

        assertTrue(foundVariants.isEmpty())
    }

    @Test
    fun barcodeExists_ExistingBarcodeProvided_ReturnTrue() = runTest {
        val variant = getSingleNewItem()
        productVariantRepository.add(variant)

        val exists = productVariantRepository.barcodeExists(variant.barcode).first()

        assertTrue(exists)
    }

    @Test
    fun barcodeExists_NonExistingBarcodeProvided_ReturnFalse() = runTest {
        val exists = productVariantRepository.barcodeExists("0000000000000").first()

        assertFalse(exists)
    }

    @Test
    fun deleteByBarcode_ValidBarcodeProvided_DeleteVariant() = runTest {
        val variant = getSingleNewItem()
        productVariantRepository.add(variant)
        val countBefore = productVariantRepository.getAll().count()

        productVariantRepository.deleteByBarcode(variant.barcode)

        val countAfter = productVariantRepository.getAll().count()
        assertEquals(countBefore - 1, countAfter)

        val deletedVariant = productVariantRepository.getByBarcode(variant.barcode).first()
        assertNull(deletedVariant)
    }

    @Test
    fun getProductWithBarcode_ValidBarcodeProvided_ReturnProductWithBarcode() = runTest {
        val variant = getSingleNewItem()
        productVariantRepository.add(variant)

        val productWithBarcode = productVariantRepository.getProductWithBarcode(variant.barcode).first()

        assertNotNull(productWithBarcode)
        assertEquals(variant.barcode, productWithBarcode.variant.barcode)
        assertNotNull(productWithBarcode.product)
    }

    @Test
    fun getProductWithBarcode_InvalidBarcodeProvided_ReturnNull() = runTest {
        val productWithBarcode = productVariantRepository.getProductWithBarcode("0000000000000").first()

        assertNull(productWithBarcode)
    }

    @Test
    fun add_TwoVariantsWithSameBarcode_ReplaceExisting() = runTest {
        val variant = getSingleNewItem()
        val variantId1 = productVariantRepository.add(variant)

        // Add a new variant with same barcode but different createdAt
        val updatedVariant = variant.copy(
            id = 0,
            createdAt = System.currentTimeMillis() + 5000
        )
        val variantId2 = productVariantRepository.add(updatedVariant)

        // Should replace existing, not add new
        val count = productVariantRepository.getAll().count()
        assertEquals(1, count)

        // Should have same barcode but potentially updated createdAt
        val foundVariant = productVariantRepository.getByBarcode(variant.barcode).first()
        assertNotNull(foundVariant)
        assertEquals(variant.barcode, foundVariant.barcode)
    }
}

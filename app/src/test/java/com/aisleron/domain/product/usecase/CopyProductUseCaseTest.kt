package com.aisleron.domain.product.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.ProductRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class CopyProductUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var copyProductUseCase: CopyProductUseCase
    private lateinit var existingProduct: Product

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val productRepository = testData.getRepository<ProductRepository>()

        copyProductUseCase = CopyProductUseCaseImpl(
            productRepository,
            testData.getRepository<AisleProductRepository>(),
            IsProductNameUniqueUseCase(productRepository)
        )

        existingProduct = runBlocking { productRepository.getAll().first() }
    }

    @Test
    fun copyProduct_IsDuplicateName_ThrowsException() = runTest {
        val existingName = "Existing Product Name"
        val productRepository = testData.getRepository<ProductRepository>()
        productRepository.add(existingProduct.copy(id = 0, name = existingName))

        assertThrows<AisleronException.DuplicateProductNameException> {
            copyProductUseCase(existingProduct, existingName)
        }
    }

    @Test
    fun copyProduct_IsValidName_ProductCreated() = runTest {
        val newName = "Copied Product Name"

        val newProductId = copyProductUseCase(existingProduct, newName)

        val newProduct = testData.getRepository<ProductRepository>().get(newProductId)
        assertNotNull(newProduct)
        assertEquals(newName, newProduct.name)

        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        val sourceAisles = aisleProductRepository.getProductAisles(existingProduct.id)
        val newAisles = aisleProductRepository.getProductAisles(newProductId)
        assertTrue(newAisles.any())
        assertEquals(sourceAisles.count(), newAisles.count())
    }

    /**
     * Tests:
     * - New Product Created
     * - Aisle Product Entries created
     * - Rank is max rank in Aisle
     */
}
package com.aisleron.domain.aisleproduct.usecase

import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisleproduct.AisleProduct
import com.aisleron.domain.aisleproduct.AisleProductRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.product.ProductRepository
import com.aisleron.data.TestDataManager
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddAisleProductsUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var addAisleProductsUseCase: AddAisleProductsUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        addAisleProductsUseCase =
            AddAisleProductsUseCase(testData.getRepository<AisleProductRepository>())
    }

    @Test
    fun addAisleProduct_IsExistingAisleProduct_AisleProductUpdated() {
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        val existingAisleProduct = runBlocking { aisleProductRepository.getAll().first() }
        val updateAisleProduct = existingAisleProduct.copy(
            rank = existingAisleProduct.rank + 10
        )
        val updatedAisleProduct: AisleProduct?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = aisleProductRepository.getAll().count()
            val id = addAisleProductsUseCase(listOf(updateAisleProduct)).first()
            updatedAisleProduct = aisleProductRepository.get(id)
            countAfter = aisleProductRepository.getAll().count()
        }
        Assertions.assertNotNull(updatedAisleProduct)
        Assertions.assertEquals(countBefore, countAfter)
        Assertions.assertEquals(updateAisleProduct.id, updatedAisleProduct?.id)
        Assertions.assertEquals(updateAisleProduct.aisleId, updatedAisleProduct?.aisleId)
        Assertions.assertEquals(updateAisleProduct.rank, updatedAisleProduct?.rank)
        Assertions.assertEquals(updateAisleProduct.product, updatedAisleProduct?.product)
    }

    private fun getNewAisleProduct(): AisleProduct {
        val newAisle = Aisle(
            name = "AisleProductTest Aisle",
            products = emptyList(),
            locationId = runBlocking {
                testData.getRepository<LocationRepository>().getAll().first().id
            },
            rank = 1,
            isDefault = false,
            id = 0
        )

        return AisleProduct(
            id = 0,
            aisleId = runBlocking { testData.getRepository<AisleRepository>().add(newAisle) },
            rank = 1,
            product = runBlocking { testData.getRepository<ProductRepository>().getAll().first() }
        )
    }

    @Test
    fun addAisleProduct_IsNewAisleProduct_AisleProductCreated() {
        val newAisleProduct = getNewAisleProduct()
        val countBefore: Int
        val countAfter: Int
        val insertedAisleProduct: AisleProduct?
        val aisleProductRepository = testData.getRepository<AisleProductRepository>()
        runBlocking {
            countBefore = aisleProductRepository.getAll().count()
            val id = addAisleProductsUseCase(listOf(newAisleProduct)).first()
            insertedAisleProduct = aisleProductRepository.get(id)
            countAfter = aisleProductRepository.getAll().count()
        }
        Assertions.assertNotNull(insertedAisleProduct)
        Assertions.assertEquals(countBefore + 1, countAfter)
        Assertions.assertEquals(newAisleProduct.product, insertedAisleProduct?.product)
        Assertions.assertEquals(newAisleProduct.aisleId, insertedAisleProduct?.aisleId)
        Assertions.assertEquals(newAisleProduct.rank, insertedAisleProduct?.rank)
    }
}
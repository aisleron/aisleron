package com.aisleron.domain.sampledata.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.product.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CreateSampleDataUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var createSampleDataUseCase: CreateSampleDataUseCase
    private lateinit var testUseCaseProvider: TestUseCaseProvider

    @BeforeEach
    fun setUp() {
        testData = TestDataManager(false)
        testUseCaseProvider = TestUseCaseProvider(testData)
        createSampleDataUseCase = testUseCaseProvider.createSampleDataUseCase
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ProductsCreated() {
        val productCountBefore = runBlocking { testData.productRepository.getAll().count() }

        runBlocking { createSampleDataUseCase() }

        val productCountAfter = runBlocking { testData.productRepository.getAll().count() }

        Assertions.assertEquals(productCountBefore, 0)
        Assertions.assertTrue(productCountBefore < productCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_HomeAislesCreated() {
        val homeId = runBlocking { testData.locationRepository.getHome().id }
        val aisleCountBefore =
            runBlocking { testData.aisleRepository.getAll().count { it.locationId == homeId } }

        runBlocking { createSampleDataUseCase() }

        val aisleCountAfter =
            runBlocking { testData.aisleRepository.getAll().count { it.locationId == homeId } }

        Assertions.assertEquals(aisleCountBefore, 1)
        Assertions.assertTrue(aisleCountBefore < aisleCountAfter)
    }

    @Test
    fun createSampleDataUseCase_HomeAislesCreated_ProductsMappedInHomeAisles() {
        runBlocking { createSampleDataUseCase() }

        val homeList = runBlocking {
            val homeId = testData.locationRepository.getHome().id
            testUseCaseProvider.getShoppingListUseCase(homeId).first()!!
        }

        val aisleProductCountAfter = homeList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_NoRestrictionsViolated_ShopCreated() {
        val shopCountBefore = runBlocking { testData.locationRepository.getShops().first().count() }

        runBlocking { createSampleDataUseCase() }

        val shopCountAfter = runBlocking { testData.locationRepository.getShops().first().count() }

        Assertions.assertEquals(shopCountBefore, 0)
        Assertions.assertTrue(shopCountBefore < shopCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ShopCreated_ProductsMappedInShopAisles() {
        runBlocking { createSampleDataUseCase() }

        val shopList = runBlocking {
            val shopId = testData.locationRepository.getShops().first().first().id
            testUseCaseProvider.getShoppingListUseCase(shopId).first()!!
        }

        val aisleProductCountAfter = shopList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ProductsExistInDatabase_ThrowsException() {
        runBlocking {
            testUseCaseProvider.addProductUseCase(
                Product(
                    id = 0,
                    name = "CreateSampleDataProductExistsTest",
                    inStock = false
                )
            )

            assertThrows<AisleronException.SampleDataCreationException> {
                createSampleDataUseCase()
            }
        }
    }

}

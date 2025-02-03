package com.aisleron.domain.sampledata.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.usecase.AddAisleUseCaseImpl
import com.aisleron.domain.aisle.usecase.GetDefaultAislesUseCase
import com.aisleron.domain.aisleproduct.usecase.AddAisleProductsUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductRankUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.usecase.AddLocationUseCaseImpl
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetLocationUseCase
import com.aisleron.domain.location.usecase.IsLocationNameUniqueUseCase
import com.aisleron.domain.product.Product
import com.aisleron.domain.product.usecase.AddProductUseCaseImpl
import com.aisleron.domain.product.usecase.GetAllProductsUseCase
import com.aisleron.domain.product.usecase.IsProductNameUniqueUseCase
import com.aisleron.domain.shoppinglist.usecase.GetShoppingListUseCase
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

    @BeforeEach
    fun setUp() {
        testData = TestDataManager(false)

        val getShoppingListUseCase = GetShoppingListUseCase(testData.locationRepository)
        val getAllProductsUseCase = GetAllProductsUseCase(testData.productRepository)
        val getHomeLocationUseCase = GetHomeLocationUseCase(testData.locationRepository)
        val addAisleProductsUseCase = AddAisleProductsUseCase(testData.aisleProductRepository)
        val updateAisleProductRankUseCase =
            UpdateAisleProductRankUseCase(testData.aisleProductRepository)

        val addProductUseCase = AddProductUseCaseImpl(
            testData.productRepository,
            GetDefaultAislesUseCase(testData.aisleRepository),
            addAisleProductsUseCase,
            IsProductNameUniqueUseCase(testData.productRepository)
        )

        val addAisleUseCase = AddAisleUseCaseImpl(
            testData.aisleRepository,
            GetLocationUseCase(testData.locationRepository)
        )

        val addLocationUseCase = AddLocationUseCaseImpl(
            testData.locationRepository,
            addAisleUseCase,
            getAllProductsUseCase,
            addAisleProductsUseCase,
            IsLocationNameUniqueUseCase(testData.locationRepository)
        )

        createSampleDataUseCase = CreateSampleDataUseCaseImpl (
            addProductUseCase = addProductUseCase,
            addAisleUseCase = addAisleUseCase,
            getShoppingListUseCase = getShoppingListUseCase,
            updateAisleProductRankUseCase = updateAisleProductRankUseCase,
            addLocationUseCase = addLocationUseCase,
            getAllProductsUseCase = getAllProductsUseCase,
            getHomeLocationUseCase = getHomeLocationUseCase
        )
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
            GetShoppingListUseCase(testData.locationRepository).invoke(homeId).first()!!
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
            GetShoppingListUseCase(testData.locationRepository).invoke(shopId).first()!!
        }

        val aisleProductCountAfter = shopList.aisles.find { !it.isDefault }?.products?.count() ?: 0

        Assertions.assertTrue(0 < aisleProductCountAfter)
    }

    @Test
    fun createSampleDataUseCase_ProductsExistInDatabase_ThrowsException() {
        runBlocking {
            val addProductUseCase = AddProductUseCaseImpl(
                testData.productRepository,
                GetDefaultAislesUseCase(testData.aisleRepository),
                AddAisleProductsUseCase(testData.aisleProductRepository),
                IsProductNameUniqueUseCase(testData.productRepository)
            )

            addProductUseCase(
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

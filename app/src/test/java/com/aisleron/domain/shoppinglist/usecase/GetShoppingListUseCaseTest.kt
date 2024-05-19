package com.aisleron.domain.shoppinglist.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.location.Location
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetShoppingListUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var getShoppingListUseCase: GetShoppingListUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        getShoppingListUseCase = GetShoppingListUseCase(testData.locationRepository)
    }

    @Test
    fun getShoppingList_NonExistentId_ReturnNull() {
        val shoppingList: Location? = runBlocking { getShoppingListUseCase(2001).first() }
        assertNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocation() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.locationRepository.getAll().first().id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertNotNull(shoppingList)
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithAisles() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.locationRepository.getAll().first().id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertTrue(shoppingList!!.aisles.isNotEmpty())
    }

    @Test
    fun getShoppingList_ExistingId_ReturnLocationWithProducts() {
        val shoppingList: Location?
        runBlocking {
            val locationId = testData.locationRepository.getAll().first().id
            shoppingList = getShoppingListUseCase(locationId).first()
        }
        assertTrue(shoppingList!!.aisles.count { it.products.isNotEmpty() } > 0)
    }
}
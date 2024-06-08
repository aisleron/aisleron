package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.usecase.GetLocationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AddAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var addAisleUseCase: AddAisleUseCase

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        addAisleUseCase = AddAisleUseCase(
            testData.aisleRepository,
            GetLocationUseCase(testData.locationRepository)
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun addAisle_IsExistingAisle_AisleUpdated() {
        val existingAisle = runBlocking { testData.aisleRepository.getAll().first() }
        val updateAisle = existingAisle.copy(
            name = existingAisle.name + " Updated"
        )
        val updatedAisle: Aisle?
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = testData.aisleRepository.getAll().count()
            val id = addAisleUseCase(updateAisle)
            updatedAisle = testData.aisleRepository.get(id)
            countAfter = testData.aisleRepository.getAll().count()
        }
        assertNotNull(updatedAisle)
        assertEquals(countBefore, countAfter)
        assertEquals(updateAisle, updatedAisle)
    }

    private fun getNewAisle(): Aisle {
        return Aisle(
            id = 0,
            name = "New Aisle 1",
            products = emptyList(),
            locationId = runBlocking { testData.locationRepository.getAll().first().id },
            rank = 1000,
            isDefault = false
        )
    }

    @Test
    fun addAisle_IsNewAisle_AisleCreated() {
        val newAisle = getNewAisle()
        val countBefore: Int
        val countAfter: Int
        val insertedAisle: Aisle?
        runBlocking {
            countBefore = testData.aisleRepository.getAll().count()
            val id = addAisleUseCase(newAisle)
            insertedAisle = testData.aisleRepository.get(id)
            countAfter = testData.aisleRepository.getAll().count()
        }
        assertNotNull(insertedAisle)
        assertEquals(countBefore + 1, countAfter)
        assertEquals(newAisle.name, insertedAisle?.name)
        assertEquals(newAisle.locationId, insertedAisle?.locationId)
        assertEquals(newAisle.rank, insertedAisle?.rank)
        assertEquals(newAisle.isDefault, insertedAisle?.isDefault)
    }

    @Test
    fun addAisle_IsNewAisle_AisleAddedToExpectedLocation() {
        val newAisle = getNewAisle()
        val countBefore: Int
        val countAfter: Int
        runBlocking {
            countBefore = testData.aisleRepository.getForLocation(newAisle.locationId).count()
            addAisleUseCase(newAisle)
            countAfter = testData.aisleRepository.getForLocation(newAisle.locationId).count()
        }
        assertEquals(countBefore + 1, countAfter)
    }

    @Test
    fun addAisle_IsInvalidLocation_ThrowsInvalidLocationException() {
        val newAisle = getNewAisle().copy(locationId = -1)
        runBlocking {
            assertThrows<AisleronException.InvalidLocationException> { addAisleUseCase(newAisle) }
        }
    }
}
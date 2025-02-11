package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.usecase.GetLocationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UpdateAisleUseCaseTest {
    private lateinit var testData: TestDataManager
    private lateinit var updateAisleUseCase: UpdateAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        val aisleRepository = testData.getRepository<AisleRepository>()

        existingAisle = runBlocking { aisleRepository.get(1)!! }
        updateAisleUseCase = UpdateAisleUseCaseImpl(
            aisleRepository, GetLocationUseCase(testData.getRepository<LocationRepository>())
        )
    }

    @Test
    fun updateAisle_AisleExists_RecordUpdated() {
        val updateAisle = existingAisle.copy(name = existingAisle.name + " Updated")
        val updatedAisle: Aisle?
        val countBefore: Int
        val countAfter: Int
        val aisleRepository = testData.getRepository<AisleRepository>()
        runBlocking {
            countBefore = aisleRepository.getAll().count()
            updateAisleUseCase(updateAisle)
            updatedAisle = aisleRepository.get(existingAisle.id)
            countAfter = aisleRepository.getAll().count()
        }
        Assertions.assertNotNull(updatedAisle)
        Assertions.assertEquals(countBefore, countAfter)
        Assertions.assertEquals(updateAisle, updatedAisle)
    }

    @Test
    fun updateAisle_AisleDoesNotExist_RecordCreated() {
        val newAisle = existingAisle.copy(
            id = 0,
            name = existingAisle.name + " Inserted",
            isDefault = false
        )
        val updatedAisle: Aisle?
        val countBefore: Int
        val countAfter: Int
        val aisleRepository = testData.getRepository<AisleRepository>()
        runBlocking {
            countBefore = aisleRepository.getAll().count()
            updateAisleUseCase(newAisle)
            val id = aisleRepository.getAll().maxOf { it.id }
            updatedAisle = aisleRepository.get(id)
            countAfter = aisleRepository.getAll().count()
        }
        Assertions.assertNotNull(updatedAisle)
        Assertions.assertEquals(countBefore + 1, countAfter)
        Assertions.assertEquals(newAisle.name, updatedAisle?.name)
        Assertions.assertEquals(newAisle.locationId, updatedAisle?.locationId)
        Assertions.assertEquals(newAisle.rank, updatedAisle?.rank)
        Assertions.assertEquals(newAisle.isDefault, updatedAisle?.isDefault)
    }

    @Test
    fun updateAisle_IsInvalidLocation_ThrowsInvalidLocationException() {
        runBlocking {
            assertThrows<AisleronException.InvalidLocationException> {
                updateAisleUseCase(existingAisle.copy(locationId = -1))
            }
        }
    }
}
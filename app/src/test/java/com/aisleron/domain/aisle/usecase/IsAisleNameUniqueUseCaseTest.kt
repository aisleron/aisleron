package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsAisleNameUniqueUseCaseTest {
    private lateinit var isAisleNameUniqueUseCase: IsAisleNameUniqueUseCase

    @BeforeEach
    fun setUp() {
        isAisleNameUniqueUseCase =
            IsAisleNameUniqueUseCase(testData.getRepository<AisleRepository>())
    }

    @Test
    fun isNameUnique_NoMatchingNameExists_ReturnTrue() = runTest {
        val newAisle = existingAisle.copy(id = 0, name = "Aisle Unique Name")

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertNotEquals(existingAisle.name, newAisle.name)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_AisleIdsMatch_ReturnTrue() = runTest {
        val newAisle = existingAisle.copy(expanded = !existingAisle.expanded)

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(existingAisle.id, newAisle.id)
        Assertions.assertTrue(result)
    }

    @Test
    fun isNameUnique_NamesMatchIdsDiffer_ReturnFalse() = runTest {
        val newAisle = existingAisle.copy(id = 0)

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(existingAisle.name, newAisle.name)
        Assertions.assertNotEquals(existingAisle.id, newAisle.id)
        Assertions.assertFalse(result)
    }

    @Test
    fun isNameUnique_NamesMatchWithDifferentCase_ReturnFalse() = runTest {
        val newAisle = existingAisle.copy(id = 0, name = "  ${existingAisle.name.uppercase()}  ")

        val result = isAisleNameUniqueUseCase(newAisle)

        Assertions.assertEquals(
            existingAisle.name.uppercase().trim(), newAisle.name.uppercase().trim()
        )

        Assertions.assertNotEquals(existingAisle.id, newAisle.id)
        Assertions.assertFalse(result)
    }

    companion object {
        private lateinit var testData: TestDataManager
        private lateinit var existingAisle: Aisle

        @JvmStatic
        @BeforeAll
        fun beforeSpec() {
            testData = TestDataManager()

            existingAisle = runBlocking {
                testData.getRepository<AisleRepository>().getAll().first { !it.isDefault }
            }
        }
    }
}
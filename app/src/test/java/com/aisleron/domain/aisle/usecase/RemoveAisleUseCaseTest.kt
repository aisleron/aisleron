package com.aisleron.domain.aisle.usecase

import com.aisleron.data.TestDataManager
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisleproduct.usecase.RemoveProductsFromAisleUseCase
import com.aisleron.domain.aisleproduct.usecase.UpdateAisleProductsUseCase
import com.aisleron.domain.base.AisleronException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RemoveAisleUseCaseTest {

    private lateinit var testData: TestDataManager
    private lateinit var removeAisleUseCase: RemoveAisleUseCase
    private lateinit var existingAisle: Aisle

    @BeforeEach
    fun setUp() {
        testData = TestDataManager()
        removeAisleUseCase = RemoveAisleUseCaseImpl(
            testData.aisleRepository,
            UpdateAisleProductsUseCase(testData.aisleProductRepository),
            RemoveProductsFromAisleUseCase(testData.aisleProductRepository),
        )

        runBlocking {
            existingAisle = testData.aisleRepository.getAll().first { !it.isDefault }
            val defaultAisle =
                testData.aisleRepository.getDefaultAisleFor(existingAisle.locationId)!!
            val aisleProducts =
                testData.aisleProductRepository.getAll().filter { it.aisleId == defaultAisle.id }

            aisleProducts.forEach {
                val moveAisle = it.copy(aisleId = existingAisle.id)
                testData.aisleProductRepository.update(moveAisle)
            }
        }
    }

    @Test
    fun removeAisle_IsDefaultAisle_ThrowsException() {
        runBlocking {
            val defaultAisle = testData.aisleRepository.getDefaultAisles().first()

            assertThrows<AisleronException.DeleteDefaultAisleException> {
                removeAisleUseCase(defaultAisle)
            }
        }
    }

    @Test
    fun removeAisle_IsExistingNonDefaultAisle_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        runBlocking {
            countBefore = testData.aisleRepository.getAll().count()
            removeAisleUseCase(existingAisle)
            removedAisle = testData.aisleRepository.get(existingAisle.id)
            countAfter = testData.aisleRepository.getAll().count()
        }
        assertNull(removedAisle)
        assertEquals(countBefore - 1, countAfter)
    }

    @Test
    fun removeAisle_HasNoDefaultAisle_RemoveAisleProducts() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCount: Int
        runBlocking {
            val defaultAisle = testData.aisleRepository.getDefaultAisleFor(existingAisle.locationId)
            defaultAisle?.let { testData.aisleRepository.remove(it) }
            aisleProductCount =
                testData.aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }

            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()

            removeAisleUseCase(existingAisle)

            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()

        }
        assertEquals(aisleProductCountBefore - aisleProductCount, aisleProductCountAfter)
    }

    @Test
    fun removeAisle_HasDefaultAisle_MoveAisleProducts() {
        val aisleProductCountBefore: Int
        val aisleProductCountAfter: Int
        val aisleProductCount: Int
        val defaultAisleProductCountBefore: Int
        val defaultAisleProductCountAfter: Int
        runBlocking {
            val defaultAisle =
                testData.aisleRepository.getDefaultAisleFor(existingAisle.locationId)!!
            aisleProductCount =
                testData.aisleProductRepository.getAll().count { it.aisleId == existingAisle.id }
            defaultAisleProductCountBefore =
                testData.aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }

            aisleProductCountBefore = testData.aisleProductRepository.getAll().count()

            removeAisleUseCase(existingAisle)

            aisleProductCountAfter = testData.aisleProductRepository.getAll().count()

            defaultAisleProductCountAfter =
                testData.aisleProductRepository.getAll().count { it.aisleId == defaultAisle.id }


        }
        assertEquals(aisleProductCountBefore, aisleProductCountAfter)
        assertEquals(defaultAisleProductCountBefore + aisleProductCount, defaultAisleProductCountAfter)
    }

    @Test
    fun removeAisle_AisleHasNoProducts_AisleRemoved() {
        val countBefore: Int
        val countAfter: Int
        val removedAisle: Aisle?
        runBlocking {
            val emptyAisleId = testData.aisleRepository.add(
                Aisle(
                    name = "Empty Aisle",
                    products = emptyList(),
                    locationId = testData.locationRepository.getAll().first().id,
                    rank = 1000,
                    id = 0,
                    isDefault = false
                )
            )
            val emptyAisle = testData.aisleRepository.get(emptyAisleId)!!
            countBefore = testData.aisleRepository.getAll().count()
            removeAisleUseCase(emptyAisle)
            removedAisle = testData.aisleRepository.get(emptyAisle.id)
            countAfter = testData.aisleRepository.getAll().count()
        }
        assertNull(removedAisle)
        assertEquals(countBefore - 1, countAfter)
    }
}
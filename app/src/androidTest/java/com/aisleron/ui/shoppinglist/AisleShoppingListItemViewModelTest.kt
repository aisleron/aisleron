package com.aisleron.ui.shoppinglist

import com.aisleron.data.TestDataManager
import com.aisleron.domain.TestUseCaseProvider
import com.aisleron.domain.aisle.Aisle
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AisleShoppingListItemViewModelTest {

    private lateinit var testData: TestDataManager
    private lateinit var testUseCases: TestUseCaseProvider

    @Before
    fun setUp() {
        testData = TestDataManager()
        testUseCases = TestUseCaseProvider(testData)
    }

    private fun getAisleShoppingListItemViewModel(existingAisle: Aisle): AisleShoppingListItemViewModel {
        return AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = existingAisle.locationId,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )
    }

    private fun getAisle(): Aisle {
        return runBlocking {
            val existingLocationId = testData.locationRepository.getAll().first().id
            testData.aisleRepository.getAll()
                .last { it.locationId == existingLocationId && !it.isDefault }
        }
    }

    @Test
    fun removeItem_ItemIsStandardAisle_AisleRemoved() {
        val existingAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(existingAisle)

        runBlocking { shoppingListItem.remove() }

        val removedAisle = runBlocking { testData.aisleRepository.get(existingAisle.id) }
        Assert.assertNull(removedAisle)
    }

    @Test
    fun removeItem_ItemIsInvalidAisle_NoAisleRemoved() {
        val shoppingListItem = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = -1,
            updateAisleRankUseCase = testUseCases.updateAisleRankUseCase,
            getAisleUseCase = testUseCases.getAisleUseCase,
            removeAisleUseCase = testUseCases.removeAisleUseCase
        )

        val aisleCountBefore = runBlocking { testData.aisleRepository.getAll().count() }
        runBlocking { shoppingListItem.remove() }
        val aisleCountAfter = runBlocking { testData.aisleRepository.getAll().count() }

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun updateItemRank_AisleMoved_AisleRankUpdated() {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)
        val precedingAisle = runBlocking {
            testData.aisleRepository.getAll()
                .first { it.locationId == movedAisle.locationId && !it.isDefault && it.id != movedAisle.id }
        }

        val precedingItem = getAisleShoppingListItemViewModel(precedingAisle)

        runBlocking { shoppingListItem.updateRank(precedingItem) }

        val updatedAisle = runBlocking { testData.aisleRepository.get(movedAisle.id) }
        Assert.assertEquals(precedingItem.rank + 1, updatedAisle?.rank)
    }

    @Test
    fun updateItemRank_NullPrecedingItem_AisleRankIsOne() {
        val movedAisle = getAisle()
        val shoppingListItem = getAisleShoppingListItemViewModel(movedAisle)

        runBlocking { shoppingListItem.updateRank(null) }

        val updatedAisle = runBlocking { testData.aisleRepository.get(movedAisle.id) }

        Assert.assertEquals(1, updatedAisle?.rank)
    }

}
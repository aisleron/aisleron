package com.aisleron.ui.aisle

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.shoppinglist.AisleShoppingListItemViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare

class AisleViewModelTest : KoinTest {
    private lateinit var aisleViewModel: AisleViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        aisleViewModel = get<AisleViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun addAisle_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on update Product Status"

        declare<AddAisleUseCase> {
            object : AddAisleUseCase {
                override suspend fun invoke(aisle: Aisle): Int {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<AisleViewModel>()

        vm.addAisle("New Dummy Aisle", 1)

        val uiState = vm.uiState.value
        Assert.assertTrue(uiState is AisleViewModel.AisleUiState.Error)
        with(uiState as AisleViewModel.AisleUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun addAisle_IsInvalidLocation_UiStateIsError() {
        val newAisleName = "Add New Aisle Test"

        aisleViewModel.addAisle(newAisleName, -1)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Error)
    }

    @Test
    fun addAisle_IsValidLocation_AisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = "Add New Aisle Test"

        aisleViewModel.addAisle(newAisleName, existingLocation.id)

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        Assert.assertNotNull(addedAisle)
        Assert.assertEquals(newAisleName, addedAisle?.name)
        Assert.assertEquals(existingLocation.id, addedAisle?.locationId)
        Assert.assertFalse(addedAisle!!.isDefault)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun addAisle_AisleNameIsBlank_NoAisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = ""

        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()
        aisleViewModel.addAisle(newAisleName, existingLocation.id)
        val aisleCountAfter = aisleRepository.getAll().count()

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun updateAisle_ExceptionRaised_UiStateIsError() {
        val exceptionMessage = "Error on update Aisle"

        declare<UpdateAisleUseCase> {
            object : UpdateAisleUseCase {
                override suspend fun invoke(aisle: Aisle) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val vm = get<AisleViewModel>()
        val sli = AisleShoppingListItemViewModel(
            rank = 1000,
            id = -1,
            name = "Dummy",
            isDefault = false,
            childCount = 0,
            locationId = 1,
            expanded = true,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        vm.updateAisleName(sli, "Dummy Dummy")

        val uiState = vm.uiState.value
        Assert.assertTrue(uiState is AisleViewModel.AisleUiState.Error)
        with(uiState as AisleViewModel.AisleUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun updateAisle_IsInvalidLocation_UiStateIsError() = runTest {
        val updatedAisleName = "Update Aisle Test"
        val existingAisle = get<AisleRepository>().getAll().first()
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            childCount = 0,
            locationId = -1,
            expanded = existingAisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        aisleViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Error)
    }

    @Test
    fun updateAisle_IsValidLocation_AisleUpdated() = runTest {
        val updatedAisleName = "Update Aisle Test"
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            locationId = existingAisle.locationId,
            childCount = 0,
            expanded = existingAisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        aisleViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNotNull(updatedAisle)
        Assert.assertEquals(existingAisle.copy(name = updatedAisleName), updatedAisle)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun updateAisle_AisleNameIsBlank_AisleNotUpdated() = runTest {
        val updatedAisleName = ""
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        val updateShoppingListItem = AisleShoppingListItemViewModel(
            rank = existingAisle.rank,
            id = existingAisle.id,
            name = existingAisle.name,
            isDefault = existingAisle.isDefault,
            locationId = existingAisle.locationId,
            childCount = 0,
            expanded = existingAisle.expanded,
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            removeAisleUseCase = get<RemoveAisleUseCase>()
        )

        val aisleCountBefore = aisleRepository.getAll().count()
        aisleViewModel.updateAisleName(updateShoppingListItem, updatedAisleName)
        val aisleCountAfter = aisleRepository.getAll().count()
        val updatedAisle = aisleRepository.get(existingAisle.id)

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
        Assert.assertNotEquals(updatedAisleName, updatedAisle?.name)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun clearState_AfterCall_StateIsEmpty() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = "Add New Aisle Test"
        aisleViewModel.addAisle(newAisleName, existingLocation.id)
        val stateBefore = aisleViewModel.uiState.value

        aisleViewModel.clearState()
        val stateAfter = aisleViewModel.uiState.value

        Assert.assertNotEquals(stateBefore, stateAfter)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Empty)
    }
}
/*
 * Copyright (C) 2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.ui.aisle

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.AddAisleUseCase
import com.aisleron.domain.aisle.usecase.GetAisleMaxRankUseCase
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

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

        vm.setAisleName("Dummy Dummy")
        vm.addAisle()

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

        aisleViewModel.hydrate(-1, -1)
        aisleViewModel.setAisleName(newAisleName)
        aisleViewModel.addAisle()

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Error)
    }

    @Test
    fun addAisle_IsValidLocation_AisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = "Add New Aisle Test"
        aisleViewModel.hydrate(-1, existingLocation.id)
        aisleViewModel.setAisleName(newAisleName)
        val aisleMaxRank = get<AisleRepository>().getAisleMaxRank(existingLocation.id)

        aisleViewModel.addAisle()

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        assertNotNull(addedAisle)
        assertEquals(newAisleName, addedAisle.name)
        assertEquals(existingLocation.id, addedAisle.locationId)
        assertFalse(addedAisle.isDefault)
        assertEquals(aisleMaxRank + 1, addedAisle.rank)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun addAisle_AisleNameIsBlank_NoAisleAdded() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = ""
        aisleViewModel.hydrate(-1, existingLocation.id)
        aisleViewModel.setAisleName(newAisleName)

        aisleViewModel.addAisle()

        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()
        aisleViewModel.addAisle()
        val aisleCountAfter = aisleRepository.getAll().count()

        Assert.assertEquals(aisleCountBefore, aisleCountAfter)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun updateAisleName_IsValidLocation_AisleUpdated() = runTest {
        val updatedAisleName = "Update Aisle Test"
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        aisleViewModel.hydrate(existingAisle.id, existingAisle.locationId)
        aisleViewModel.setAisleName(updatedAisleName)

        aisleViewModel.updateAisleName()

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
        aisleViewModel.hydrate(existingAisle.id, existingAisle.locationId)
        aisleViewModel.setAisleName(updatedAisleName)


        val aisleCountBefore = aisleRepository.getAll().count()
        aisleViewModel.updateAisleName()

        val aisleCountAfter = aisleRepository.getAll().count()
        Assert.assertEquals(aisleCountBefore, aisleCountAfter)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNotEquals(updatedAisleName, updatedAisle?.name)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun clearState_AfterCall_StateIsEmpty() = runTest {
        val existingLocation = get<LocationRepository>().getAll().first()
        val newAisleName = "Add New Aisle Test"
        aisleViewModel.hydrate(-1, existingLocation.id)
        aisleViewModel.setAisleName(newAisleName)
        aisleViewModel.addAisle()
        val stateBefore = aisleViewModel.uiState.value

        aisleViewModel.clearState()

        val stateAfter = aisleViewModel.uiState.value
        Assert.assertNotEquals(stateBefore, stateAfter)
        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Empty)
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_aisleViewModelReturned() {
        val vm = AisleViewModel(
            get<AddAisleUseCase>(),
            get<UpdateAisleUseCase>(),
            get<GetAisleUseCase>(),
            get<GetAisleMaxRankUseCase>(),
        )

        Assert.assertNotNull(vm)
    }

    @Test
    fun hydrate_isValidAisle_AisleNamePopulated() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }

        aisleViewModel.hydrate(aisle.id, aisle.locationId)

        assertEquals(aisle.name, aisleViewModel.aisleName.value)
    }

    @Test
    fun hydrate_isInvalidAisle_AisleNameIsBlank() = runTest {
        aisleViewModel.hydrate(-1, -1)

        assertEquals("", aisleViewModel.aisleName.value)
    }

    @Test
    fun hydrate_isSameAisleId_DoNotRehydrate() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        aisleViewModel.hydrate(aisle.id, aisle.locationId)
        val newAisleName = "My New Aisle Name"
        aisleViewModel.setAisleName(newAisleName)

        aisleViewModel.hydrate(aisle.id, aisle.locationId)

        assertEquals(newAisleName, aisleViewModel.aisleName.value)
    }

    @Test
    fun updateAisleName_ExceptionRaised_UiStateIsError() = runTest {
        val exceptionMessage = "Error on update Aisle"

        declare<UpdateAisleUseCase> {
            object : UpdateAisleUseCase {
                override suspend fun invoke(aisle: Aisle) {
                    throw Exception(exceptionMessage)
                }
            }
        }

        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        val vm = get<AisleViewModel>()
        vm.hydrate(aisle.id, aisle.locationId)
        vm.setAisleName("Dummy Dummy")

        vm.updateAisleName()

        val uiState = vm.uiState.value
        Assert.assertTrue(uiState is AisleViewModel.AisleUiState.Error)
        with(uiState as AisleViewModel.AisleUiState.Error) {
            Assert.assertEquals(AisleronException.ExceptionCode.GENERIC_EXCEPTION, this.errorCode)
            Assert.assertEquals(exceptionMessage, this.errorMessage)
        }
    }

    @Test
    fun updateAisleName_IsValidAisle_AisleUpdated() = runTest {
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        aisleViewModel.hydrate(existingAisle.id, existingAisle.locationId)

        val updatedAisleName = "Update Aisle Test"
        aisleViewModel.setAisleName(updatedAisleName)
        aisleViewModel.updateAisleName()

        val updatedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertNotNull(updatedAisle)
        Assert.assertEquals(existingAisle.copy(name = updatedAisleName), updatedAisle)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }

    @Test
    fun updateAisleName_AisleNameIsBlank_AisleNotUpdated() = runTest {
        val updatedAisleName = ""
        val aisleRepository = get<AisleRepository>()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        val aisleCountBefore = aisleRepository.getAll().count()
        aisleViewModel.hydrate(existingAisle.id, existingAisle.locationId)

        aisleViewModel.setAisleName(updatedAisleName)
        aisleViewModel.updateAisleName()

        val aisleCountAfter = aisleRepository.getAll().count()
        Assert.assertEquals(aisleCountBefore, aisleCountAfter)

        val updatedAisle = aisleRepository.get(existingAisle.id)
        Assert.assertEquals(existingAisle.name, updatedAisle?.name)

        Assert.assertTrue(aisleViewModel.uiState.value is AisleViewModel.AisleUiState.Success)
    }
}
/*
 * Copyright (C) 2025-2026 aisleron.com
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

package com.aisleron.ui.settings

import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import com.aisleron.domain.location.Location
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.location.usecase.GetHomeLocationUseCase
import com.aisleron.domain.location.usecase.GetPinnedShopsUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import kotlinx.coroutines.flow.first
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

class SettingsViewModelTest : KoinTest {
    private lateinit var settingsViewModel: SettingsViewModel

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @Before
    fun setUp() {
        declare<DatabaseMaintenance> { DatabaseMaintenanceDbNameTestImpl("Dummy") }
        settingsViewModel = get<SettingsViewModel>()
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }

    @Test
    fun constructor_NoCoroutineScopeProvided_SettingsViewModelReturned() {
        val vm = SettingsViewModel(
            backupDatabaseUseCase = get<BackupDatabaseUseCase>(),
            restoreDatabaseUseCase = get<RestoreDatabaseUseCase>(),
            getHomeLocationUseCase = get<GetHomeLocationUseCase>(),
            getPinnedShopsUseCase = get<GetPinnedShopsUseCase>()
        )

        Assert.assertNotNull(vm)
    }

    @Test
    fun requestLocationDetails_OnRun_ReturnHomeLocationId() = runTest {
        val homeLocation = get<LocationRepository>().getHome()

        settingsViewModel.requestLocationDetails()

        val result =
            settingsViewModel.uiState.value as SettingsViewModel.UiState.LocationListUpdated

        assertEquals(homeLocation.id, result.homeLocationId)
    }

    private fun requestLocationDetailsTest(location: Location) {
        val expectedOption = StartLocationOption(
            id = location.id,
            name = location.name,
            filterType = location.defaultFilter
        )

        settingsViewModel.requestLocationDetails()

        val locationOption =
            (settingsViewModel.uiState.value as SettingsViewModel.UiState.LocationListUpdated)
                .locationOptions.single { it.id == location.id }

        assertEquals(expectedOption, locationOption)
    }

    private suspend fun getLocationWithFilterType(filterType: FilterType): Location {
        val locationRepository = get<LocationRepository>()
        val location =
            locationRepository.getPinnedShops().first().first { it.type == LocationType.SHOP }
                .copy(defaultFilter = filterType)

        locationRepository.update(location)
        return location
    }

    @Test
    fun requestLocationDetails_LocationHasNeededFilter_ReturnWithNeededFilter() = runTest {
        val location = getLocationWithFilterType(FilterType.NEEDED)
        requestLocationDetailsTest(location)
    }

    @Test
    fun requestLocationDetails_LocationHasInStockFilter_ReturnWithInStockFilter() = runTest {
        val location = getLocationWithFilterType(FilterType.IN_STOCK)
        requestLocationDetailsTest(location)
    }

    @Test
    fun requestLocationDetails_LocationHasAllFilter_ReturnWithAllFilter() = runTest {
        val location = getLocationWithFilterType(FilterType.ALL)
        requestLocationDetailsTest(location)
    }

    /**
     * Tests:
     *  shops returned with correct filter type
     */

}
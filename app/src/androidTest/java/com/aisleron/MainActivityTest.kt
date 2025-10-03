/*
 * Copyright (C) 2025 aisleron.com
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

package com.aisleron

import androidx.appcompat.app.AppCompatDelegate
import androidx.test.core.app.ActivityScenario
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals

class MainActivityTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            viewModelTestModule,
            repositoryModule,
            useCaseModule,
            generalTestModule,
            preferenceTestModule
        )
    )

    @Before
    fun setUp() {
        SharedPreferencesInitializer().clearPreferences()
    }

    @Test
    fun appStart_LightThemeSet_UseLightTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME)

        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun appStart_DarkThemeSet_UseDarkTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.DARK_THEME)
        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
    }

    @Test
    fun appStart_SystemThemeSet_UseSystemTheme() {
        SharedPreferencesInitializer().setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.SYSTEM_THEME)
        ActivityScenario.launch(MainActivity::class.java)
        assertEquals(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.getDefaultNightMode()
        )
    }

    private fun appStart_TestHomeListOptions(filterType: FilterType, expectedResId: Int){
        declare<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
        val locationId = 1

        with(SharedPreferencesInitializer()) {
            setIsInitialized(true)
            setStartingList(locationId, filterType)
        }

        val activity = ActivityScenario.launch(MainActivity::class.java)
        activity.onActivity {
            assertEquals(it.getString(expectedResId), it.supportActionBar?.title)
        }
    }

    @Test
    fun appStart_StartPageIsInStock_ShowInStock() {
        appStart_TestHomeListOptions(FilterType.IN_STOCK, R.string.menu_in_stock)
    }

    @Test
    fun appStart_StartPageIsNeeded_ShowNeeded() {
        appStart_TestHomeListOptions(FilterType.NEEDED, R.string.menu_needed)
    }

    @Test
    fun appStart_StartPageIsAll_ShowAll() {
        appStart_TestHomeListOptions(FilterType.ALL, R.string.menu_all_items)
    }

    @Test
    fun appStart_StartPageIsShop_ShowShop() = runTest {
        declare<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
        get<CreateSampleDataUseCase>().invoke()
        val location = get<LocationRepository>().getShops().first().first()

        with(SharedPreferencesInitializer()) {
            setIsInitialized(true)
            setStartingList(location.id, location.defaultFilter)
        }

        val activity = ActivityScenario.launch(MainActivity::class.java)
        activity.onActivity {
            assertEquals(location.name, it.supportActionBar?.title)
        }
    }
}
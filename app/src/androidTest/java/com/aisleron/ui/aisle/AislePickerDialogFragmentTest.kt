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

package com.aisleron.ui.aisle

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.aisleron.AppCompatActivityTestImpl
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.aisle.Aisle
import com.aisleron.domain.aisle.usecase.GetAislesForLocationUseCase
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.bundles.AisleListEntry
import com.aisleron.ui.bundles.AislePickerBundle
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AislePickerDialogFragmentTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @JvmField
    @Rule
    val activityScenarioRule = ActivityScenarioRule(AppCompatActivityTestImpl::class.java)

    private lateinit var getAislesForLocationUseCase: GetAislesForLocationUseCase
    private lateinit var locationRepository: LocationRepository
    private lateinit var aisles: List<Aisle>
    private lateinit var aislePickerBundle: AislePickerBundle

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
        getAislesForLocationUseCase = get()
        locationRepository = get()
        runBlocking {
            val location = locationRepository.getAll().first()
            aisles = getAislesForLocationUseCase(location.id)
            aislePickerBundle = AislePickerBundle(
                "Test Aisle Picker",
                aisles.map { AisleListEntry(it.id, it.name) },
                aisles.first().id
            )
        }
    }

    @Test
    fun show_dialog_displaysCorrectTitleAndAisles() {
        activityScenarioRule.scenario.onActivity {
            val dialog = AislePickerDialogFragment.newInstance(aislePickerBundle, "requestKey")
            dialog.show(it.supportFragmentManager, AislePickerDialogFragment.TAG)
        }

        onView(withText("Test Aisle Picker"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        aisles.forEach {
            onView(withText(it.name))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun show_dialog_correctAisleIsChecked() {
        activityScenarioRule.scenario.onActivity {
            val dialog = AislePickerDialogFragment.newInstance(aislePickerBundle, "requestKey")
            dialog.show(it.supportFragmentManager, AislePickerDialogFragment.TAG)
        }

        onView(withText(aisles.first().name))
            .inRoot(isDialog())
            .check(matches(isChecked()))

        onView(withText(aisles.last().name))
            .inRoot(isDialog())
            .check(matches(not(isChecked())))
    }

    @Test
    fun selectAisle_setsFragmentResultWithSelectedId() {
        val requestKey = "selectAisle"
        var selectedAisleId = -1
        val expectedAisle = aisles[1]

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, bundle ->
                selectedAisleId = bundle.getInt(AislePickerDialogFragment.KEY_SELECTED_AISLE_ID)
            }

            val dialog = AislePickerDialogFragment.newInstance(aislePickerBundle, requestKey)
            dialog.show(activity.supportFragmentManager, AislePickerDialogFragment.TAG)
        }

        onView(withText(expectedAisle.name))
            .inRoot(isDialog())
            .perform(click())

        assertEquals(expectedAisle.id, selectedAisleId)
    }

    @Test
    fun clickAddNewAisle_setsFragmentResultForAdding() {
        val requestKey = "addNewAisle"
        var addNewAisle = false

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, bundle ->
                addNewAisle = bundle.getBoolean(AislePickerDialogFragment.KEY_ADD_NEW_AISLE)
            }

            val dialog = AislePickerDialogFragment.newInstance(aislePickerBundle, requestKey)
            dialog.show(activity.supportFragmentManager, AislePickerDialogFragment.TAG)
        }

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .perform(click())

        assertTrue(addNewAisle)
    }

    @Test
    fun clickCancel_dismissesDialogWithoutResult() {
        val requestKey = "cancel"
        var listenerCalled = false

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, _ ->
                listenerCalled = true
            }

            val dialog = AislePickerDialogFragment.newInstance(aislePickerBundle, requestKey)
            dialog.show(activity.supportFragmentManager, AislePickerDialogFragment.TAG)
        }

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        assertFalse(listenerCalled, "Fragment result listener should not be called on cancel.")
    }
}

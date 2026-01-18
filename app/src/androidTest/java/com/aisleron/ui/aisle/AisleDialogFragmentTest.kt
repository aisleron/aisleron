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

package com.aisleron.ui.aisle

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.bundles.AisleDialogBundle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AisleDialogFragmentTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(daoTestModule, viewModelTestModule, repositoryModule, useCaseModule)
    )

    @JvmField
    @Rule
    val activityScenarioRule = ActivityScenarioRule(AppCompatActivityTestImpl::class.java)

    @Before
    fun setUp() {
        runBlocking { get<CreateSampleDataUseCase>().invoke() }
    }


    private suspend fun showAddMultipleDialog(requestKey: String = "addAisles") {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        showAddMultipleDialog(location.id, requestKey)
    }

    private fun showAddMultipleDialog(locationId: Int, requestKey: String = "addAisles") {
        val aisleDialogBundle = AisleDialogBundle(
            aisleId = -1,
            action = AisleDialogFragment.AisleDialogAction.ADD_MULTIPLE,
            locationId = locationId
        )

        activityScenarioRule.scenario.onActivity {
            val dialog = AisleDialogFragment.newInstance(aisleDialogBundle, requestKey)
            dialog.show(it.supportFragmentManager, AisleDialogFragment.TAG)
        }
    }

    @Test
    fun newInstance_ActionIsAddMultiple_AddMultipleAislesDialogShown() = runTest {
        showAddMultipleDialog()

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText("")))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun addMultiAisleDoneClick_HasAisleName_NewAisleAdded() = runTest {
        val requestKey = "addAisles"
        var aisleId = -1

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, bundle ->
                aisleId = bundle.getInt(AisleDialogFragment.KEY_AISLE_ID)
            }
        }

        showAddMultipleDialog(requestKey)

        val newAisleName = "Add Aisle Test 123321"
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }
        assertNotNull(addedAisle)
        assertEquals(addedAisle.id, aisleId)
    }

    @Test
    fun addAisleAddAnotherClick_HasAisleName_AisleAddedAndShowDialogRemains() = runTest {
        showAddMultipleDialog()
        val newAisleName = "Add Aisle Test 123321"
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }
        assertNotNull(addedAisle)
    }

    @Test
    fun addAisleCancelClick_Always_NoAisleAdded() = runTest {
        val newAisleName = "Add Aisle Test 123321"

        showAddMultipleDialog()
        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore, aisleCountAfter)

        val addedAisle = aisleRepository.getAll().firstOrNull { it.name == newAisleName }
        assertNull(addedAisle)
    }

    @Test
    fun addAisleDoneClick_AisleNameIsEmpty_NoAisleAdded() = runTest {
        showAddMultipleDialog()
        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    @Test
    fun addAisleDoneClick_DuplicateAisleName_ShowError() = runTest {
        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()
        val existingAisle = aisleRepository.getAll().first { !it.isDefault }
        showAddMultipleDialog(existingAisle.locationId)

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(existingAisle.name))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(R.string.duplicate_aisle_name_exception))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        val aisleCountAfter = aisleRepository.getAll().count()
        assertEquals(aisleCountBefore, aisleCountAfter)
    }

    private fun showEditDialog(aisle: Aisle, requestKey: String = "editAisle") {
        val aisleDialogBundle = AisleDialogBundle(
            aisleId = aisle.id,
            action = AisleDialogFragment.AisleDialogAction.EDIT,
            locationId = aisle.locationId
        )

        activityScenarioRule.scenario.onActivity {
            val dialog = AisleDialogFragment.newInstance(aisleDialogBundle, requestKey)
            dialog.show(it.supportFragmentManager, AisleDialogFragment.TAG)
        }
    }

    @Test
    fun newInstance_ActionIsEdit_EditAisleDialogShown() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        showEditDialog(aisle)

        onView(withText(R.string.edit_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText(aisle.name)))
    }

    @Test
    fun editAisleCancelClick_Always_AisleNotUpdated() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        showEditDialog(aisle)

        val updateSuffix = " Updated"
        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(updateSuffix))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)

        assertEquals(aisle.name, updatedAisle?.name)
    }

    @Test
    fun editAisleDoneClick_HasAisleName_AisleUpdated() = runTest {
        val requestKey = "editAisle"
        var aisleId = -1

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, bundle ->
                aisleId = bundle.getInt(AisleDialogFragment.KEY_AISLE_ID)
            }
        }

        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        showEditDialog(aisle, requestKey)

        val updatedAisleName = "Updated Aisle Name"
        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(replaceText(updatedAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)
        assertEquals(updatedAisleName, updatedAisle?.name)
        assertEquals(updatedAisle?.id, aisleId)
    }

    @Test
    fun editAisleDoneClick_AisleNameIsEmpty_AisleNotUpdated() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        showEditDialog(aisle)

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(clearText())

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)
        assertEquals(aisle.name, updatedAisle?.name)
    }

    @Test
    fun editAisleDoneClick_DuplicateAisleName_ShowError() = runTest {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        showEditDialog(aisle)

        val nameToDuplicate = get<AisleRepository>().getAll()
            .first { !it.isDefault && it.locationId == aisle.locationId && it.id != aisle.id }.name

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(replaceText(nameToDuplicate))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        onView(withText(R.string.duplicate_aisle_name_exception))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        val updatedAisle = get<AisleRepository>().get(aisle.id)
        assertEquals(aisle.name, updatedAisle?.name)
    }

    private suspend fun showAddSingleDialog(requestKey: String = "addAisle") {
        val location = get<LocationRepository>().getAll().first { it.type == LocationType.SHOP }
        showAddSingleDialog(location.id, requestKey)
    }

    private fun showAddSingleDialog(locationId: Int, requestKey: String = "addAisle") {
        val aisleDialogBundle = AisleDialogBundle(
            aisleId = -1,
            action = AisleDialogFragment.AisleDialogAction.ADD_SINGLE,
            locationId = locationId
        )

        activityScenarioRule.scenario.onActivity {
            val dialog = AisleDialogFragment.newInstance(aisleDialogBundle, requestKey)
            dialog.show(it.supportFragmentManager, AisleDialogFragment.TAG)
        }
    }

    @Test
    fun newInstance_ActionIsAddSingle_AddSingleAisleDialogShown() = runTest {
        showAddSingleDialog()

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText("")))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .check(doesNotExist())
    }

    @Test
    fun addSingleAisleDoneClick_HasAisleName_NewAisleAdded() = runTest {
        val requestKey = "addAisle"
        var aisleId = -1

        activityScenarioRule.scenario.onActivity { activity ->
            activity.supportFragmentManager.setFragmentResultListener(
                requestKey, activity
            ) { _, bundle ->
                aisleId = bundle.getInt(AisleDialogFragment.KEY_AISLE_ID)
            }
        }

        showAddSingleDialog(requestKey)

        val newAisleName = "Add Aisle Test 123321"
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }
        assertNotNull(addedAisle)
        assertEquals(addedAisle.id, aisleId)
    }

    @Test
    fun addAisle_AfterError_ErrorClearedWhenTyping() = runTest {
        // Get an existing aisle to create a duplicate
        val existingAisle = get<AisleRepository>().getAll().first { !it.isDefault }

        // Show dialog and trigger error by entering a duplicate name
        showAddSingleDialog(existingAisle.locationId)
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(existingAisle.name))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        // Verify error is shown
        onView(withText(R.string.duplicate_aisle_name_exception))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        // Type in the edit box to clear the error
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(clearText(), typeText("New Aisle Name"))

        // Verify error message is cleared
        onView(withText(R.string.duplicate_aisle_name_exception))
            .check(doesNotExist())
    }
}
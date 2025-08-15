package com.aisleron.ui.aisle

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
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
import com.aisleron.domain.aisle.AisleRepository
import com.aisleron.domain.aisle.usecase.GetAisleUseCase
import com.aisleron.domain.aisle.usecase.RemoveAisleUseCase
import com.aisleron.domain.aisle.usecase.UpdateAisleRankUseCase
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.ui.shoppinglist.AisleShoppingListItem
import com.aisleron.ui.shoppinglist.AisleShoppingListItemViewModel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class AisleDialogImplTest : KoinTest {
    private lateinit var dialog: AisleDialogImpl

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
        dialog = AisleDialogImpl(get<AisleViewModel>())
    }

    private fun showAddDialog(locationId: Int) {
        activityScenarioRule.scenario.onActivity {
            dialog.observeLifecycle(it)
            dialog.showAddDialog(it, locationId)
        }
    }

    private fun showEditDialog(aisle: AisleShoppingListItem) {
        activityScenarioRule.scenario.onActivity {
            dialog.observeLifecycle(it)
            dialog.showEditDialog(it, aisle)
        }
    }

    @Test
    fun showAddDialog_IsCalled_AddAisleDialogShown() {
        showAddDialog(1)

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText("")))
    }

    @Test
    fun addAisleDoneClick_HasAisleName_NewAisleAdded() = runTest {
        val newAisleName = "Add Aisle Test 123321"

        showAddDialog(1)
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        assertNotNull(addedAisle)
    }

    @Test
    fun addAisleAddAnotherClick_HasAisleName_AisleAddedAndShowDialogRemains() = runTest {
        val newAisleName = "Add Aisle Test 123321"

        showAddDialog(1)
        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(R.string.add_another))
            .inRoot(isDialog())
            .perform(click())

        val addedAisle = get<AisleRepository>().getAll().firstOrNull { it.name == newAisleName }

        onView(withText(R.string.add_aisle))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        assertNotNull(addedAisle)
    }

    @Test
    fun addAisleCancelClick_Always_NoAisleAdded() = runTest {
        val newAisleName = "Add Aisle Test 123321"

        showAddDialog(1)
        val aisleRepository = get<AisleRepository>()
        val aisleCountBefore = aisleRepository.getAll().count()

        onView(allOf(withId(R.id.edt_aisle_name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(typeText(newAisleName))

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val aisleCountAfter = aisleRepository.getAll().count()
        val addedAisle = aisleRepository.getAll().firstOrNull { it.name == newAisleName }

        assertEquals(aisleCountBefore, aisleCountAfter)
        assertNull(addedAisle)
    }

    @Test
    fun addAisleDoneClick_AisleNameIsEmpty_NoAisleAdded() = runTest {
        showAddDialog(1)
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
        showAddDialog(existingAisle.locationId)
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

    private suspend fun getAisleItem(): AisleShoppingListItem {
        val aisle = get<AisleRepository>().getAll().first { !it.isDefault }
        return AisleShoppingListItemViewModel(
            rank = aisle.rank,
            id = aisle.id,
            name = aisle.name,
            isDefault = aisle.isDefault,
            locationId = aisle.locationId,
            removeAisleUseCase = get<RemoveAisleUseCase>(),
            getAisleUseCase = get<GetAisleUseCase>(),
            updateAisleRankUseCase = get<UpdateAisleRankUseCase>(),
            expanded = aisle.expanded
        )
    }

    @Test
    fun showEditDialog_IsCalled_EditAisleDialogShown() = runTest {
        val aisle = getAisleItem()
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
        val updateSuffix = " Updated"
        val aisle = getAisleItem()
        showEditDialog(aisle)

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
        val updatedAisleName = "Updated Aisle Name"
        val aisle = getAisleItem()
        showEditDialog(aisle)

        onView(allOf(withText(aisle.name), instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .perform(replaceText(updatedAisleName))

        onView(withText(R.string.done))
            .inRoot(isDialog())
            .perform(click())

        val updatedAisle = get<AisleRepository>().get(aisle.id)

        assertEquals(updatedAisleName, updatedAisle?.name)
    }

    @Test
    fun editAisleDoneClick_AisleNameIsEmpty_AisleNotUpdated() = runTest {
        val aisle = getAisleItem()
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
        val aisle = getAisleItem()
        val nameToDuplicate = get<AisleRepository>().getAll()
            .first { !it.isDefault && it.locationId == aisle.locationId && it.id != aisle.id }.name

        showEditDialog(aisle)

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
}
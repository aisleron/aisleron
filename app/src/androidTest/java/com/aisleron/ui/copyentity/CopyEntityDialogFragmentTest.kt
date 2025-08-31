package com.aisleron.ui.copyentity

import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
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
import com.aisleron.domain.product.ProductRepository
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CopyEntityDialogFragmentTest : KoinTest {

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

    @Test
    fun newInstance_IsCalled_ReturnsCopyDialog() {
        val title = "Copy Entity XYZ"
        val defaultName = "Entity XYZ (Copy)"
        val nameHint = "New Entity Name"

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        assertNotNull(dialog)
    }

    @Test
    fun show_IsCalled_DisplaysDefaultValues() {
        val title = "Copy Entity XYZ"
        val defaultName = "Entity XYZ (Copy)"
        val nameHint = "New Entity Name"

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "copyDialog")
        }

        onView(withText(title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(allOf(instanceOf(EditText::class.java)))
            .inRoot(isDialog())
            .check(matches(withText(defaultName)))
    }

    @Test
    fun cancel_IsCalled_EntityNotCopied() = runTest {
        val entity = get<ProductRepository>().getAll().first()
        val title = "Copy ${entity.name}"
        val defaultName = "${entity.name} (Copy)"
        val nameHint = "New Product Name"
        var copySuccess = false

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        dialog.onCopySuccess = { copySuccess = true }
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "copyDialog")
        }

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .perform(click())

        val copiedEntity = get<ProductRepository>().getByName(defaultName)
        assertNull(copiedEntity)
        assertFalse(copySuccess)
    }

    @Test
    fun ok_HasError_ShowError() = runTest {
        val entity = get<ProductRepository>().getAll().first()
        val title = "Copy ${entity.name}"
        val defaultName = entity.name
        val nameHint = "New Product Name"
        var copySuccess = false

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        dialog.onCopySuccess = { copySuccess = true }
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "copyDialog")
        }

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        assertFalse(copySuccess)
        onView(withText(R.string.duplicate_product_name_exception))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun ok_ValidEntity_EntityCopied() = runTest {
        val entity = get<ProductRepository>().getAll().first()
        val title = "Copy ${entity.name}"
        val defaultName = "${entity.name} (Copy)"
        val nameHint = "New Product Name"
        var copySuccess = false

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        dialog.onCopySuccess = { copySuccess = true }
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "copyDialog")
        }

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        val copiedEntity = get<ProductRepository>().getByName(defaultName)
        assertNotNull(copiedEntity)
        assertTrue(copySuccess)
    }

    @Test
    fun ok_NameIsBlank_ShowError() = runTest {
        val title = "Copy Entity"
        val defaultName = ""
        val nameHint = "New Product Name"
        var copySuccess = false

        val dialog = CopyEntityDialogFragment.newInstance(
            CopyEntityType.Product(1), title, defaultName, nameHint
        )

        dialog.onCopySuccess = { copySuccess = true }
        activityScenarioRule.scenario.onActivity {
            dialog.show(it.supportFragmentManager, "copyDialog")
        }

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .perform(click())

        assertFalse(copySuccess)
        onView(withText(R.string.entity_name_required))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }
}
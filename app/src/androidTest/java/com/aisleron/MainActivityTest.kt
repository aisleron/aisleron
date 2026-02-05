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

package com.aisleron

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.factoryModule
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
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MainActivityTest : KoinTest {

    private lateinit var sharedPreferencesInitializer: SharedPreferencesInitializer

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            viewModelTestModule,
            repositoryModule,
            useCaseModule,
            generalTestModule,
            preferenceTestModule,
            factoryModule
        )
    )

    @Before
    fun setUp() {
        sharedPreferencesInitializer = SharedPreferencesInitializer()
        sharedPreferencesInitializer.clearPreferences()
    }

    private fun runThemeTest(
        theme: SharedPreferencesInitializer.ApplicationTheme, expectedMode: Int
    ) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_UNSPECIFIED)
        sharedPreferencesInitializer.setApplicationTheme(theme)

        ActivityScenario.launch(MainActivity::class.java).use {
            assertEquals(expectedMode, AppCompatDelegate.getDefaultNightMode())
        }
    }

    @Test
    fun appStart_lightThemeSet_appliesLightTheme() {
        runThemeTest(
            SharedPreferencesInitializer.ApplicationTheme.LIGHT_THEME,
            AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    @Test
    fun appStart_darkThemeSet_appliesDarkTheme() {
        runThemeTest(
            SharedPreferencesInitializer.ApplicationTheme.DARK_THEME,
            AppCompatDelegate.MODE_NIGHT_YES
        )
    }

    @Test
    fun appStart_systemThemeSet_appliesSystemTheme() {
        runThemeTest(
            SharedPreferencesInitializer.ApplicationTheme.SYSTEM_THEME,
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }

    private fun runHomeListOptionTest(filterType: FilterType, expectedResId: Int) {
        declare<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
        sharedPreferencesInitializer.setIsInitialized(true)
        sharedPreferencesInitializer.setStartingList(1, filterType)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals(activity.getString(expectedResId), activity.supportActionBar?.title)
            }
        }
    }

    @Test
    fun appStart_homeListIsInStock_showsInStockTitle() {
        runHomeListOptionTest(FilterType.IN_STOCK, R.string.menu_in_stock)
    }

    @Test
    fun appStart_homeListIsNeeded_showsNeededTitle() {
        runHomeListOptionTest(FilterType.NEEDED, R.string.menu_needed)
    }

    @Test
    fun appStart_homeListIsAll_showsAllTitle() {
        runHomeListOptionTest(FilterType.ALL, R.string.menu_all_items)
    }

    @Test
    fun appStart_StartPageIsShop_ShowShop() = runTest {
        declare<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
        get<CreateSampleDataUseCase>().invoke()
        val location = get<LocationRepository>().getShops().first().first()

        sharedPreferencesInitializer.setIsInitialized(true)
        sharedPreferencesInitializer.setStartingList(location.id, location.defaultFilter)

        val activity = ActivityScenario.launch(MainActivity::class.java)
        activity.onActivity {
            assertEquals(location.name, it.supportActionBar?.title)
        }
    }

    private fun runUpdateBannerTest(
        isInitialized: Boolean,
        lastUpdateCode: Int,
        lastUpdateName: String,
        expectedVisibility: ViewMatchers.Visibility
    ) {
        sharedPreferencesInitializer.setIsInitialized(isInitialized)
        sharedPreferencesInitializer.setLastUpdateCode(lastUpdateCode)
        sharedPreferencesInitializer.setLastUpdateName(lastUpdateName)

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.update_banner)).check(
                matches(ViewMatchers.withEffectiveVisibility(expectedVisibility))
            )
        }
    }

    @Test
    fun onCreate_updateIsAvailable_showsUpdateBanner() {
        runUpdateBannerTest(
            isInitialized = true,
            lastUpdateCode = 1,
            lastUpdateName = "2025.1.0",
            expectedVisibility = ViewMatchers.Visibility.VISIBLE
        )
    }

    @Test
    fun onCreate_versionIsCurrent_hidesUpdateBanner() {
        runUpdateBannerTest(
            isInitialized = true,
            lastUpdateCode = BuildConfig.VERSION_CODE,
            lastUpdateName = BuildConfig.VERSION_NAME,
            expectedVisibility = ViewMatchers.Visibility.GONE
        )
    }

    @Test
    fun onCreate_appNotInitialized_hidesUpdateBanner() {
        runUpdateBannerTest(
            isInitialized = false,
            lastUpdateCode = 1,
            lastUpdateName = "2025.1.0",
            expectedVisibility = ViewMatchers.Visibility.GONE
        )
    }

    @Test
    fun onClick_Dismiss_hidesBannerAndUpdatesPreferences() {
        // Setup: banner is visible
        sharedPreferencesInitializer.setIsInitialized(true)
        sharedPreferencesInitializer.setLastUpdateCode(1)
        sharedPreferencesInitializer.setLastUpdateName("2025.1.0")

        ActivityScenario.launch(MainActivity::class.java).use {
            // Action
            onView(withId(R.id.btn_update_dismiss)).perform(click())

            // Assertions
            // 1. Banner is gone
            onView(withId(R.id.update_banner)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )
        }

        // 2. Preferences are updated
        val sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
        val updateCodeAfter = sharedPrefs.getInt("last_update_code", 0)
        assertEquals(BuildConfig.VERSION_CODE, updateCodeAfter)
        val updateNameAfter = sharedPrefs.getString("last_update_name", "")
        assertEquals(BuildConfig.VERSION_NAME, updateNameAfter)
    }

    @Test
    fun onClick_ViewChanges_showsChangelogHidesBannerAndUpdatesPreferences() {
        // Setup: banner is visible
        sharedPreferencesInitializer.setIsInitialized(true)
        sharedPreferencesInitializer.setLastUpdateCode(1)
        sharedPreferencesInitializer.setLastUpdateName("2025.1.0")

        var uri: Uri? = null

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                uri = it.getString(R.string.aisleron_version_history_url).toUri()
            }

            Intents.init()
            intending(
                allOf(hasAction(Intent.ACTION_VIEW), hasData(uri!!))
            ).respondWith(
                Instrumentation.ActivityResult(Activity.RESULT_OK, null)
            )

            // Action
            onView(withId(R.id.btn_update_view_changes)).perform(click())

            // Assertions
            // 1. Intent was sent
            intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(uri)))
            // 2. Banner is gone
            onView(withId(R.id.update_banner)).check(
                matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE))
            )

            Intents.release()
        }

        // 3. Preferences are updated
        val sharedPrefs =
            PreferenceManager.getDefaultSharedPreferences(getInstrumentation().targetContext)
        val updateCodeAfter = sharedPrefs.getInt("last_update_code", 0)
        assertEquals(BuildConfig.VERSION_CODE, updateCodeAfter)
        val updateNameAfter = sharedPrefs.getString("last_update_name", "")
        assertEquals(BuildConfig.VERSION_NAME, updateNameAfter)
    }

    private fun Context.getColorFromTheme(@AttrRes attrRes: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    private fun runDynamicColorTest(enabled: Boolean) {
        sharedPreferencesInitializer.setDynamicColor(enabled)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val activityColor = activity.getColorFromTheme(android.R.attr.colorPrimary)

                val baselineContext =
                    ContextThemeWrapper(activity, R.style.Theme_Aisleron)
                val baselineColor =
                    baselineContext.getColorFromTheme(android.R.attr.colorPrimary)

                val shouldApplyDynamicColors =
                    enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

                if (shouldApplyDynamicColors) {
                    assertNotEquals(
                        baselineColor, activityColor,
                        "Dynamic color should have been applied, but colorPrimary matches the baseline."
                    )
                } else {
                    assertEquals(
                        baselineColor, activityColor,
                        "Dynamic color should NOT have been applied, but colorPrimary does not match the baseline."
                    )
                }
            }
        }
    }

    @Test
    fun appStart_dynamicColorsEnabled_areApplied() {
        runDynamicColorTest(true)
    }

    @Test
    fun appStart_dynamicColorsDisabled_areNotApplied() {
        runDynamicColorTest(false)
    }

    private fun assertPostQBehavior(
        activity: MainActivity,
        style: SharedPreferencesInitializer.PureBlackStyle,
        expectedBlackness: Map<Int, Boolean>
    ) {
        val baselineContext = ContextThemeWrapper(activity, R.style.Theme_Aisleron)
        expectedBlackness.forEach { (attr, shouldBeBlack) ->
            val activityColor = activity.getColorFromTheme(attr)

            if (shouldBeBlack) {
                assertEquals(
                    Color.BLACK, activityColor,
                    "Color for attribute $attr should be black for ${style.name} style."
                )
            } else {
                val baselineColor = baselineContext.getColorFromTheme(attr)
                assertEquals(
                    baselineColor, activityColor,
                    "Color for attribute $attr should match baseline for ${style.name} style."
                )
            }
        }
    }

    private fun assertPreQBehavior(
        activity: MainActivity,
        style: SharedPreferencesInitializer.PureBlackStyle,
        expectedBlackness: Map<Int, Boolean>
    ) {
        val baselineContext = ContextThemeWrapper(activity, R.style.Theme_Aisleron)

        if (style == SharedPreferencesInitializer.PureBlackStyle.DEFAULT) {
            // For default style on pre-Q, setBackgroundColor is NOT called.
            // So, we assert that the THEME ATTRIBUTE for the background is unchanged.
            val activityColor = activity.getColorFromTheme(android.R.attr.colorBackground)
            val baselineColor = baselineContext.getColorFromTheme(android.R.attr.colorBackground)
            assertEquals(
                baselineColor,
                activityColor,
                "Background attribute should match baseline on pre-Q for Default style."
            )
        } else {
            // For other styles on pre-Q, setBackgroundColor IS called.
            // So, we check the view's actual background drawable color.
            val contentView = activity.findViewById<View>(android.R.id.content)
            val background = (contentView.background as ColorDrawable).color
            assertEquals(
                Color.BLACK,
                background,
                "Background should be black on pre-Q for ${style.name} style."
            )
        }

        // For ALL styles on pre-Q, verify other theme attributes are NOT changed
        // because applyStyle() is never called.
        expectedBlackness.keys.filter { it != android.R.attr.colorBackground }.forEach { attr ->
            val activityColor = activity.getColorFromTheme(attr)
            val baselineColor = baselineContext.getColorFromTheme(attr)
            assertEquals(
                baselineColor,
                activityColor,
                "Attribute $attr should not change on pre-Q devices."
            )
        }
    }

    private fun runPureBlackTest(
        style: SharedPreferencesInitializer.PureBlackStyle,
        expectedBlackness: Map<Int, Boolean>
    ) {
        sharedPreferencesInitializer.setApplicationTheme(SharedPreferencesInitializer.ApplicationTheme.DARK_THEME)
        sharedPreferencesInitializer.setPureBlackStyle(style)

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    assertPreQBehavior(activity, style, expectedBlackness)
                } else {
                    assertPostQBehavior(activity, style, expectedBlackness)
                }
            }
        }
    }

    @Test
    fun appStart_pureBlackIsDefault_appliesDefaultColors() {
        val expected = mapOf(
            android.R.attr.colorBackground to false,
            com.google.android.material.R.attr.colorSurface to false,
            com.google.android.material.R.attr.colorSurfaceVariant to false,
            com.google.android.material.R.attr.colorSurfaceContainer to false,
            com.google.android.material.R.attr.colorSurfaceContainerLow to false,
            com.google.android.material.R.attr.colorSurfaceContainerHigh to false
        )
        runPureBlackTest(SharedPreferencesInitializer.PureBlackStyle.DEFAULT, expected)
    }

    @Test
    fun appStart_pureBlackIsEconomy_appliesEconomyColors() {
        val expected = mapOf(
            android.R.attr.colorBackground to true,
            com.google.android.material.R.attr.colorSurface to false,
            com.google.android.material.R.attr.colorSurfaceVariant to false,
            com.google.android.material.R.attr.colorSurfaceContainer to false,
            com.google.android.material.R.attr.colorSurfaceContainerLow to false,
            com.google.android.material.R.attr.colorSurfaceContainerHigh to false
        )
        runPureBlackTest(SharedPreferencesInitializer.PureBlackStyle.ECONOMY, expected)
    }

    @Test
    fun appStart_pureBlackIsBusiness_appliesBusinessColors() {
        val expected = mapOf(
            android.R.attr.colorBackground to true,
            com.google.android.material.R.attr.colorSurface to true,
            com.google.android.material.R.attr.colorSurfaceVariant to true,
            com.google.android.material.R.attr.colorSurfaceContainer to true,
            com.google.android.material.R.attr.colorSurfaceContainerLow to false,
            com.google.android.material.R.attr.colorSurfaceContainerHigh to false
        )
        runPureBlackTest(SharedPreferencesInitializer.PureBlackStyle.BUSINESS_CLASS, expected)
    }

    @Test
    fun appStart_pureBlackIsFirstClass_appliesFirstClassColors() {
        val expected = mapOf(
            android.R.attr.colorBackground to true,
            com.google.android.material.R.attr.colorSurface to true,
            com.google.android.material.R.attr.colorSurfaceVariant to true,
            com.google.android.material.R.attr.colorSurfaceContainer to true,
            com.google.android.material.R.attr.colorSurfaceContainerLow to true,
            com.google.android.material.R.attr.colorSurfaceContainerHigh to true
        )
        runPureBlackTest(SharedPreferencesInitializer.PureBlackStyle.FIRST_CLASS, expected)
    }

    private suspend fun launchWithShoppingList(): ActivityScenario<MainActivity> {
        get<CreateSampleDataUseCase>().invoke()
        val location = get<LocationRepository>().getShops().first().first()
        sharedPreferencesInitializer.setIsInitialized(true)
        sharedPreferencesInitializer.setStartingList(location.id, FilterType.ALL)
        return ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun onActionModeStarted_toolbarIsCleared() = runTest {
        val scenario = launchWithShoppingList()
        var actionBar: ActionBar? = null
        scenario.onActivity { actionBar = it.supportActionBar }
        assertTrue((actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE) != 0)

        // Long click to start action mode
        onView(withId(R.id.frg_shopping_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, longClick()
            )
        )

        // Assert toolbar state is cleared
        assertEquals(0, (actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE))
        onView(withId(R.id.action_search)).check(doesNotExist())
    }

    @Test
    fun onActionModeFinished_toolbarIsRestored() = runTest {
        val scenario = launchWithShoppingList()
        var actionBar: ActionBar? = null
        scenario.onActivity { actionBar = it.supportActionBar }

        // Start and finish action mode
        onView(withId(R.id.frg_shopping_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, longClick()
            )
        )
        pressBack()

        // Assert toolbar state is restored
        assertTrue((actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE) != 0)
        onView(withId(R.id.action_search)).check(matches(isDisplayed()))
    }

    @Test
    fun onActionMode_selectAdditionalItem_toolbarRemainsCleared() = runTest {
        var actionBar: ActionBar? = null
        val scenario = launchWithShoppingList()
        scenario.onActivity { actionBar = it.supportActionBar }

        // Start action mode and select another item
        onView(withId(R.id.frg_shopping_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, longClick()
            )
        )
        onView(withId(R.id.frg_shopping_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                1, longClick()
            )
        )

        // Assert toolbar state is still cleared
        assertEquals(0, (actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE))
        onView(withId(R.id.action_search)).check(doesNotExist())
    }

    @Test
    fun onActionMode_NavigateToDifferentPAge_ActionModeEnded() = runTest {
        var actionBar: ActionBar? = null
        val scenario = launchWithShoppingList()
        scenario.onActivity { actionBar = it.supportActionBar }
        assertTrue((actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE) != 0)

        // Long click to start action mode
        onView(withId(R.id.frg_shopping_list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0, longClick()
            )
        )

        navigateToAllItemsList(R.id.nav_needed)

        assertNotEquals(0, (actionBar!!.displayOptions and ActionBar.DISPLAY_SHOW_TITLE))
        onView(withId(R.id.action_search)).check(matches(isDisplayed()))
    }

    private fun openNavigationDrawer() {
        onView(withId(R.id.drawer_layout))
            .perform(DrawerActions.open())
    }

    private fun navigateToAllItemsList(@IdRes homeListId: Int) {
        openNavigationDrawer()
        onView(withId(homeListId))
            .perform(click())
    }
}

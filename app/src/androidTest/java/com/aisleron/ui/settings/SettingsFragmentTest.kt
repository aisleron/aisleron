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

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.AppCompatActivityTestImpl
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.factoryModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import com.aisleron.utils.SystemIds
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.Calendar
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SettingsFragmentTest : KoinTest {
    private val fragmentTag = "SETTINGS_FRAGMENT"

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            repositoryModule,
            useCaseModule,
            viewModelTestModule,
            generalTestModule,
            preferenceTestModule,
            factoryModule
        )
    )

    @Before
    fun setUp() {
        declare<DatabaseMaintenance> { DatabaseMaintenanceDbNameTestImpl("Dummy") }
    }

    @After
    fun tearDown() {
        SharedPreferencesInitializer().clearPreferences()
    }

    private fun clickOption(viewTextResourceId: Int) {
        onView(withId(SystemIds.PREFERENCE_RECYCLER_VIEW))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(viewTextResourceId)),
                    click()
                )
            )
    }

    private fun getFragmentScenario(): FragmentScenario<SettingsFragment> =
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { SettingsFragment() }
        )

    private fun getActivityScenario(): ActivityScenario<AppCompatActivityTestImpl> {
        val scenario = ActivityScenario.launch(AppCompatActivityTestImpl::class.java)
        scenario.onActivity { activity ->
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment(), fragmentTag)
                .commitNow()
        }

        return scenario
    }

    @Test
    fun onBackPressed_OnSettingsFragment_ReturnToMain() {
        var navController: NavController? = null
        var startDestination: NavDestination? = null
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            scenario.onActivity {
                navController = it.findNavController(R.id.nav_host_fragment_content_main)
                startDestination = navController.currentDestination
                navController.navigate(R.id.nav_settings)
            }
            //pressBack()
            val backAction = onView(
                Matchers.allOf(withContentDescription("Navigate up"), isDisplayed())
            )
            backAction.perform(click())

            assertEquals(startDestination, navController?.currentDestination)
        }
    }

    @Test
    fun onBackupFolderClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()

        clickOption(R.string.backup_folder)
        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))

        Intents.release()
    }

    @Test
    fun onBackupFolderClick_OnFilePickerIntentResponse_BackupFolderPreferenceUpdated() {
        val testUri = "DummyUriBackupFolder"
        var preference: Preference? = null

        getFragmentScenario().onFragment { fragment ->
            preference =
                fragment.findPreference(SettingsFragment.PreferenceOption.BACKUP_FOLDER.key)
        }

        val summaryBefore = preference?.summary
        runFilePickerIntent(testUri, Intent.ACTION_OPEN_DOCUMENT_TREE, R.string.backup_folder)

        assertNotEquals(summaryBefore, preference?.summary)
        assertEquals(testUri, preference?.summary)
    }

    private fun runFilePickerIntent(
        testUri: String, intentAction: String, viewTextResourceId: Int
    ) {
        val intent = Intent()
        intent.data = Uri.parse(testUri)
        val result: Instrumentation.ActivityResult =
            Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        Intents.init()
        intending(hasAction(intentAction)).respondWith(result)
        clickOption(viewTextResourceId)
        Intents.release()
    }

    @Test
    fun onBackupDatabaseClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()

        clickOption(R.string.backup_database)
        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))

        Intents.release()
    }

    @Test
    fun onBackupDatabaseClick_OnFilePickerIntentResponse_BackupDatabasePreferenceUpdated() {
        val testUri = "DummyUriBackupDatabase"
        var preference: Preference? = null
        var summaryPrefix = String()
        getFragmentScenario().onFragment { fragment ->
            preference =
                fragment.findPreference(SettingsFragment.PreferenceOption.BACKUP_DATABASE.key)
            summaryPrefix = fragment.getString(R.string.last_backup)
        }

        val summaryBefore = preference?.summary
        runFilePickerIntent(testUri, Intent.ACTION_OPEN_DOCUMENT_TREE, R.string.backup_database)
        val summaryAfter = preference?.summary!!

        val year = Calendar.getInstance().get(Calendar.YEAR).toString()
        assertNotEquals(summaryBefore, summaryAfter)
        assertTrue(summaryAfter.contains(Regex("$summaryPrefix.*$year.*")))
    }

    @Test
    fun onRestoreDatabaseClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()

        clickOption(R.string.restore_database)

        intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))

        Intents.release()
    }

    @Test
    fun onRestoreDatabaseClick_OnFilePickerIntentResponse_ConfirmationModalDisplayed() {
        var restoreConfirmMessage = String()
        val dbName = "Database-123.db"

        getFragmentScenario().onFragment { fragment ->
            restoreConfirmMessage = fragment.getString(R.string.db_restore_confirmation, dbName)
        }

        runFilePickerIntent(
            dbName, Intent.ACTION_OPEN_DOCUMENT, R.string.restore_database
        )

        onView(withText(restoreConfirmMessage))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onRestoreDatabaseClick_OnConfirmRestore_RestoreDatabasePreferenceUpdated() {
        val testUri = "Database-123.db"
        var preference: Preference? = null
        var summaryPrefix = String()
        getFragmentScenario().onFragment { fragment ->
            preference =
                fragment.findPreference(SettingsFragment.PreferenceOption.RESTORE_DATABASE.key)
            summaryPrefix = fragment.getString(R.string.last_restore)
        }

        val summaryBefore = preference?.summary
        runFilePickerIntent(testUri, Intent.ACTION_OPEN_DOCUMENT, R.string.restore_database)

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val summaryAfter = preference?.summary!!

        val year = Calendar.getInstance().get(Calendar.YEAR).toString()
        assertNotEquals(summaryBefore, summaryAfter)
        assertTrue(summaryAfter.contains(Regex("$summaryPrefix.*$year.*")))
    }

    @Test
    fun onRestoreDatabaseClick_OnCancelRestore_RestoreDatabasePreferenceNoUpdated() {
        val testUri = "Database-123.db"
        var preference: Preference? = null
        getFragmentScenario().onFragment { fragment ->
            preference =
                fragment.findPreference(SettingsFragment.PreferenceOption.RESTORE_DATABASE.key)
        }

        val summaryBefore = preference?.summary
        runFilePickerIntent(testUri, Intent.ACTION_OPEN_DOCUMENT, R.string.restore_database)

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        val summaryAfter = preference?.summary!!

        assertEquals(summaryBefore, summaryAfter)
    }

    @Test
    fun onFilePickerResponse_IsError_ShowErrorSnackBar() {
        val testUri = String()
        getFragmentScenario()

        runFilePickerIntent(testUri, Intent.ACTION_OPEN_DOCUMENT, R.string.restore_database)

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(SystemIds.SNACKBAR_TEXT)).check(
            matches(
                allOf(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(startsWith("ERROR:"))
                )
            )
        )
    }

    @Test
    fun onThemeClick_SelectValue_PreferenceUpdated() {
        var themePreference: ListPreference? = null
        var lightThemeText = ""
        getFragmentScenario().onFragment { fragment ->
            themePreference = fragment.findPreference("application_theme")
            lightThemeText = fragment.getString(R.string.light_theme)
        }

        // Open the dialog, select "Light" theme, and verify the change
        clickOption(R.string.theme)
        onView(withText(R.string.light_theme)).inRoot(isDialog()).perform(click())

        assertEquals(lightThemeText, themePreference?.summary)
    }

    @Test
    fun onThemeClick_CancelDialog_PreferenceNotUpdated() {
        var themePreference: ListPreference? = null
        getFragmentScenario().onFragment { fragment ->
            themePreference = fragment.findPreference("application_theme")
        }
        val summaryBefore = themePreference?.summary

        // Open the dialog and cancel it
        clickOption(R.string.theme)
        onView(withText(android.R.string.cancel)).inRoot(isDialog()).check(matches(isDisplayed()))
            .perform(click())

        // Verify the theme preference has not changed
        assertEquals(summaryBefore, themePreference?.summary)
    }

    @Test
    fun onOtherPreferenceDialog_displaysDefaultDialog() {
        getFragmentScenario().onFragment { fragment ->
            val editTextPreference = EditTextPreference(fragment.requireContext()).apply {
                key = "test_edit_text_pref"
                title = "Test Edit Text"
            }
            fragment.preferenceScreen.addPreference(editTextPreference)
        }

        onView(withId(SystemIds.PREFERENCE_RECYCLER_VIEW))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Edit Text")),
                    click()
                )
            )

        onView(withId(android.R.id.edit))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    private fun getLocalizedString(resourceId: Int, localeTag: String): String {
        val config =
            Configuration(getInstrumentation().targetContext.resources.configuration)

        config.setLocale(Locale.forLanguageTag(localeTag))
        val localizedContext =
            getInstrumentation().targetContext.createConfigurationContext(
                config
            )

        return localizedContext.resources.getString(resourceId)
    }

    private fun languageChange_ArrangeActAssert(
        scenario: ActivityScenario<AppCompatActivityTestImpl>,
        @StringRes languageResId: Int,
        localeTag: String
    ) {
        val context = getInstrumentation().targetContext
        val languageName = context.getString(languageResId)

        clickOption(R.string.language)
        onView(withText(languageName)).inRoot(isDialog()).perform(click())

        scenario.onActivity { activity ->
            val activityFragment =
                activity.supportFragmentManager.findFragmentByTag(fragmentTag) as SettingsFragment

            val languagePreference = activityFragment.findPreference<ListPreference>("language")
            assertEquals(languageName, languagePreference?.summary)
        }

        val expectedText = getLocalizedString(R.string.language, localeTag)
        onView(withText(expectedText)).check(matches(isDisplayed()))
    }

    @Test
    fun onLanguageClick_SelectValue_PreferenceUpdated() {
        val languages = listOf(
            Pair(R.string.language_english_en, "en"),
            Pair(R.string.language_afrikaans_af, "af"),
            Pair(R.string.language_bulgarian_bg, "bg"),
            Pair(R.string.language_german_de, "de"),
            Pair(R.string.language_spanish_es, "es"),
            Pair(R.string.language_french_fr, "fr"),
            Pair(R.string.language_italian_it, "it"),
            Pair(R.string.language_polish_pl, "pl"),
            Pair(R.string.language_russian_ru, "ru"),
            Pair(R.string.language_swedish_sv, "sv"),
            Pair(R.string.language_turkish_tr, "tr"),
            Pair(R.string.language_ukrainian_uk, "uk")
        )

        val scenario = getActivityScenario()
        try {
            languages.forEach { (languageResId, localeTag) ->
                try {
                    languageChange_ArrangeActAssert(scenario, languageResId, localeTag)
                } catch (e: Exception) {
                    throw AssertionError("Failed to change language to $localeTag", e)
                }
            }
        } finally {
            // Reset the system-level locale while activity is still active
            getInstrumentation().runOnMainSync {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            }

            scenario.close()
        }
    }
}

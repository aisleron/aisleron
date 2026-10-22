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
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.recyclerview.widget.RecyclerView
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
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.SharedPreferencesInitializer
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.FilterType
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.location.LocationRepository
import com.aisleron.domain.location.LocationType
import com.aisleron.domain.sampledata.usecase.CreateSampleDataUseCase
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import com.aisleron.testdata.ui.settings.LocaleDelegateTestImpl
import com.aisleron.ui.navigation.MainNavigator
import com.aisleron.ui.navigation.MainNavigatorTestImpl
import com.aisleron.utils.SystemIds
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.startsWith
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import java.util.Calendar
import java.util.Locale
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SettingsFragmentTest : KoinTest {
    private lateinit var navigator: MainNavigatorTestImpl
    private lateinit var localeDelegate: LocaleDelegateTestImpl

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            viewModelTestModule,
            useCaseModule,
            repositoryModule,
            daoTestModule,
            generalTestModule
        )
    )

    @Before
    fun setUp() {
        navigator = get<MainNavigator>() as MainNavigatorTestImpl
        localeDelegate = get<LocaleDelegate>() as LocaleDelegateTestImpl
        declare<DatabaseMaintenance> { DatabaseMaintenanceDbNameTestImpl("Dummy") }
    }

    @After
    fun tearDown() {
        SharedPreferencesInitializer().clearPreferences()
    }

    private fun clickOption(viewTextResourceId: Int) {
        onView(withId(SystemIds.PREFERENCE_RECYCLER_VIEW))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(viewTextResourceId))
                )
            )

        onView(withText(viewTextResourceId))
            .perform(click())
    }

    private fun getFragmentScenario(): FragmentScenario<SettingsFragment> =
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { SettingsFragment(navigator, localeDelegate) }
        )


    @Test
    fun onBackupFolderClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()
        try {
            clickOption(R.string.backup_folder)
            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
        } finally {
            Intents.release()
        }
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
        try {
            intending(hasAction(intentAction)).respondWith(result)
            clickOption(viewTextResourceId)
        } finally {
            Intents.release()
        }
    }

    @Test
    fun onBackupDatabaseClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()
        try {
            clickOption(R.string.backup_database)
            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT_TREE))
        } finally {
            Intents.release()
        }
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
        try {
            clickOption(R.string.restore_database)
            intended(hasAction(Intent.ACTION_OPEN_DOCUMENT))
        } finally {
            Intents.release()
        }
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
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Test Edit Text"))
                )
            )

        onView(withText("Test Edit Text"))
            .perform(click())

        onView(withId(android.R.id.edit))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun onLanguageClick_SelectValue_PreferenceUpdated() {
        val languageResId = R.string.language_spanish_es
        getFragmentScenario().use { scenario ->
            val context = getInstrumentation().targetContext
            val languageName = context.getString(languageResId)

            clickOption(R.string.language)
            onView(withText(languageName)).inRoot(isDialog()).perform(click())

            scenario.onFragment { fragment ->
                val languagePreference = fragment.findPreference<ListPreference>("language")
                assertEquals(languageName, languagePreference?.summary)
            }

            val expectedLocaleTag = "es"
            assertEquals(expectedLocaleTag, localeDelegate.getLocaleTag())
        }
    }

    private fun startingListChange_ArrangeActAssert(
        scenario: FragmentScenario<SettingsFragment>, optionText: String, expectedResult: String
    ) {
        var themePreference: ListPreference? = null
        scenario.onFragment { fragment ->
            themePreference = fragment.findPreference("starting_list")
        }

        clickOption(R.string.starting_list)
        onView(withText(optionText)).inRoot(isDialog()).perform(click())

        assertEquals(optionText, themePreference?.summary)
        assertEquals(expectedResult, themePreference?.value)
    }

    @Test
    fun startingList_SelectValue_PreferenceUpdated() = runTest {
        get<CreateSampleDataUseCase>().invoke()
        val shop =
            get<LocationRepository>().getAll().first { it.type == LocationType.SHOP && it.pinned }

        val context = getInstrumentation().targetContext
        val startListOptions = listOf(
            Pair(context.getString(R.string.menu_in_stock), "1|${FilterType.IN_STOCK.name}|"),
            Pair(context.getString(R.string.menu_needed), "1|${FilterType.NEEDED.name}|"),
            Pair(context.getString(R.string.menu_all_items), "1|${FilterType.ALL.name}|"),
            Pair(shop.name, "${shop.id}|${shop.defaultFilter.name}|"),
            Pair(
                context.getString(R.string.menu_all_shops),
                "|${FilterType.NEEDED.name}|${LocationType.SHOP}"
            )
        )

        getFragmentScenario().use { scenario ->
            startListOptions.forEach { (option, expectedResult) ->
                try {
                    startingListChange_ArrangeActAssert(scenario, option, expectedResult)
                } catch (e: Exception) {
                    throw AssertionError("Failed to set starting list to $option", e)
                }
            }
        }
    }

    @Test
    fun verifyEveryCompiledLocaleHasAnEntryInLanguageCodes() {
        // Test that all translations have corresponding config in Settings for language selection
        val context = getInstrumentation().targetContext
        val uiLanguageCodes = context.resources.getStringArray(R.array.language_codes).toSet()
        val defaultText = context.resources.getString(R.string.language)

        val missingLanguages = mutableListOf<String>()
        Locale.getISOLanguages().forEach { isoLanguage ->
            if (isoLanguage != "en") {
                val config = Configuration(context.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(isoLanguage))
                }

                val localizedContext = context.createConfigurationContext(config)
                val localizedText = localizedContext.resources.getString(R.string.language)
                val stringFileExists = localizedText != defaultText

                if (stringFileExists && !uiLanguageCodes.contains(isoLanguage)) {
                    missingLanguages.add(isoLanguage)
                }
            }
        }

        assertTrue(
            "Language Config Missing: $missingLanguages",
            missingLanguages.isEmpty()
        )
    }

    @Test
    fun onAccountSyncClick_NavigateToAccountSync() {
        getFragmentScenario().use {
            clickOption(R.string.account_sync)

            val expectedDestination =
                MainNavigatorTestImpl.TestDestination.AccountPreferencesDestination

            assertEquals(expectedDestination, navigator.destination)
        }
    }
}

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

package com.aisleron.ui.settings

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.aisleron.MainActivity
import com.aisleron.R
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.fragmentModule
import com.aisleron.di.generalTestModule
import com.aisleron.di.preferenceTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.Calendar
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class SettingsFragmentTest : KoinTest {

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule,
            fragmentModule,
            repositoryModule,
            useCaseModule,
            viewModelTestModule,
            generalTestModule,
            preferenceTestModule
        )
    )

    @Before
    fun setUp() {
        declare<DatabaseMaintenance> { DatabaseMaintenanceDbNameTestImpl("Dummy") }
    }

    private fun getFragmentScenario(): FragmentScenario<SettingsFragment> =
        launchFragmentInContainer<SettingsFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = { SettingsFragment() }
        )

    @Test
    fun onBackPressed_OnSettingsFragment_ReturnToMain() {
        var navController: NavController? = null
        var startDestination: NavDestination? = null
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            scenario.onActivity {
                navController = it.findNavController(R.id.nav_host_fragment_content_main)
                startDestination = navController?.currentDestination
                navController?.navigate(R.id.nav_settings)
            }
            //pressBack()
            val backAction = onView(
                Matchers.allOf(withContentDescription("Navigate up"), isDisplayed())
            )
            backAction.perform(click())

            Assert.assertEquals(startDestination, navController?.currentDestination)
        }
    }

    @Test
    fun onBackupFolderClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()

        onView(withText(R.string.backup_folder)).perform(click())
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
        intent.setData(Uri.parse(testUri))
        val result: Instrumentation.ActivityResult =
            Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        Intents.init()
        intending(hasAction(intentAction)).respondWith(result)
        onView(withText(viewTextResourceId)).perform(click())
        Intents.release()
    }

    @Test
    fun onBackupDatabaseClick_OnLaunchIntent_IsOpenDocumentTree() {
        getFragmentScenario()
        Intents.init()

        onView(withText(R.string.backup_database)).perform(click())
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

        onView(withText(R.string.restore_database)).perform(click())
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

        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(
                allOf(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                    withText(startsWith("ERROR:"))
                )
            )
        )
    }

    /**
     * UiState Success
     * UiState Error
     * UiState Loading Null
     * UIState Loading Message
     */
}
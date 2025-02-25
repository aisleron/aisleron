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

import android.net.Uri
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.aisleron.di.KoinTestRule
import com.aisleron.di.daoTestModule
import com.aisleron.di.repositoryModule
import com.aisleron.di.useCaseModule
import com.aisleron.di.viewModelTestModule
import com.aisleron.domain.backup.DatabaseMaintenance
import com.aisleron.domain.backup.usecase.BackupDatabaseUseCase
import com.aisleron.domain.backup.usecase.RestoreDatabaseUseCase
import com.aisleron.domain.base.AisleronException
import com.aisleron.testdata.data.maintenance.DatabaseMaintenanceDbNameTestImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import java.net.URI

@RunWith(value = Parameterized::class)
class SettingsViewModelTest(
    private val preferenceOption: SettingsFragment.PreferenceOption,
    private val clazz: Class<BackupRestoreDbPreferenceHandler>,
    private val setPreferenceValue: String,
    private val getPreferenceValue: String
) : KoinTest {
    private lateinit var preference: Preference

    @get:Rule
    val koinTestRule = KoinTestRule(
        modules = listOf(
            daoTestModule, repositoryModule, useCaseModule, viewModelTestModule
        )
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf(
                    SettingsFragment.PreferenceOption.BACKUP_FOLDER,
                    BackupFolderPreferenceHandler::class.java,
                    "Set Backup Folder Value",
                    "Get Backup Folder Value"
                ),
                arrayOf(
                    SettingsFragment.PreferenceOption.BACKUP_DATABASE,
                    BackupDbPreferenceHandler::class.java,
                    "Set Backup Database Value",
                    "Get Backup Database Value"
                ),
                arrayOf(
                    SettingsFragment.PreferenceOption.RESTORE_DATABASE,
                    RestoreDbPreferenceHandler::class.java,
                    "Set Restore Database Value",
                    "Get Restore Database Value"
                )
            )
        }
    }

    @Before
    fun setUp() {
        val pm = PreferenceManager(ApplicationProvider.getApplicationContext())
        val ps = pm.createPreferenceScreen(pm.context)
        pm.setPreferences(ps)

        preference = Preference(pm.context)
        preference.key = preferenceOption.key
        ps.addPreference(preference)

        declare<DatabaseMaintenance> { DatabaseMaintenanceDbNameTestImpl("Dummy") }
    }

    private fun getTestSettingsViewModel(): SettingsViewModel {
        return get<SettingsViewModel>()
    }

    @Test
    fun preferenceHandlerFactory_forGivenPreference_ReturnPreferenceHandler() {
        val vm = getTestSettingsViewModel()

        val preferenceHandler = vm.preferenceHandlerFactory(preferenceOption, preference)

        Assert.assertTrue(preferenceHandler.javaClass == clazz)
    }

    @Test
    fun setPreferenceValue_ValueProvided_PreferenceValueMatches() {
        val vm = getTestSettingsViewModel()
        vm.preferenceHandlerFactory(preferenceOption, preference)

        vm.setPreferenceValue(preferenceOption, setPreferenceValue)

        val preferenceValue = preference.sharedPreferences?.getString(preference.key, "")
        Assert.assertEquals(setPreferenceValue, preferenceValue)
    }

    @Test
    fun getPreferenceValue_ValueRequested_PreferenceValueMatches() {
        preference.sharedPreferences?.edit()
            ?.putString(preference.key, getPreferenceValue)
            ?.apply()

        val vm = getTestSettingsViewModel()
        vm.preferenceHandlerFactory(preferenceOption, preference)

        val preferenceValue = vm.getPreferenceValue(preferenceOption)

        Assert.assertEquals(getPreferenceValue, preferenceValue)
    }

    @Test
    fun handleOnPreferenceClick_OnClickSuccessful_UiStateIsSuccess() {
        val vm = getTestSettingsViewModel()
        val preferenceHandler = vm.preferenceHandlerFactory(preferenceOption, preference)

        vm.handleOnPreferenceClick(preferenceOption, Uri.parse("DummyUri"))

        Assert.assertEquals(
            SettingsViewModel.UiState.Success(preferenceHandler.getSuccessMessage()),
            vm.uiState.value
        )

        Assert.assertEquals(
            preferenceHandler.getSuccessMessage(),
            (vm.uiState.value as SettingsViewModel.UiState.Success).message
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun handleOnPreferenceClick_AisleronExceptionRaised_UiStateIsError() {
        val errorMessage = "${preferenceOption.key} Aisleron Exception"
        val vm = SettingsViewModel(
            object : BackupDatabaseUseCase {
                override suspend fun invoke(backupFolderUri: URI) {
                    throw AisleronException.InvalidDbNameException(errorMessage)
                }
            },

            object : RestoreDatabaseUseCase {
                override suspend fun invoke(restoreFileUri: URI) {
                    throw AisleronException.InvalidDbNameException(errorMessage)
                }
            },

            TestScope(UnconfinedTestDispatcher())
        )

        val preferenceHandler = vm.preferenceHandlerFactory(preferenceOption, preference)

        vm.handleOnPreferenceClick(preferenceOption, Uri.parse("DummyUri"))

        if (preferenceOption == SettingsFragment.PreferenceOption.BACKUP_FOLDER) {
            Assert.assertEquals(
                SettingsViewModel.UiState.Success(preferenceHandler.getSuccessMessage()),
                vm.uiState.value
            )
        } else {
            Assert.assertEquals(
                SettingsViewModel.UiState.Error(
                    AisleronException.ExceptionCode.INVALID_DB_NAME_EXCEPTION, errorMessage
                ),
                vm.uiState.value
            )

            Assert.assertEquals(
                errorMessage,
                (vm.uiState.value as SettingsViewModel.UiState.Error).errorMessage
            )

            Assert.assertEquals(
                AisleronException.ExceptionCode.INVALID_DB_NAME_EXCEPTION,
                (vm.uiState.value as SettingsViewModel.UiState.Error).errorCode
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun handleOnPreferenceClick_ExceptionRaised_UiStateIsError() {
        val errorMessage = "${preferenceOption.key} Exception"
        val vm = SettingsViewModel(
            object : BackupDatabaseUseCase {
                override suspend fun invoke(backupFolderUri: URI) {
                    throw Exception(errorMessage)
                }
            },

            object : RestoreDatabaseUseCase {
                override suspend fun invoke(restoreFileUri: URI) {
                    throw Exception(errorMessage)
                }
            },

            TestScope(UnconfinedTestDispatcher())
        )

        val preferenceHandler = vm.preferenceHandlerFactory(preferenceOption, preference)

        vm.handleOnPreferenceClick(preferenceOption, Uri.parse("DummyUri"))

        if (preferenceOption == SettingsFragment.PreferenceOption.BACKUP_FOLDER) {
            Assert.assertEquals(
                SettingsViewModel.UiState.Success(preferenceHandler.getSuccessMessage()),
                vm.uiState.value
            )
        } else {
            Assert.assertEquals(
                SettingsViewModel.UiState.Error(
                    AisleronException.ExceptionCode.GENERIC_EXCEPTION, errorMessage
                ),
                vm.uiState.value
            )

            Assert.assertEquals(
                errorMessage,
                (vm.uiState.value as SettingsViewModel.UiState.Error).errorMessage
            )

            Assert.assertEquals(
                AisleronException.ExceptionCode.GENERIC_EXCEPTION,
                (vm.uiState.value as SettingsViewModel.UiState.Error).errorCode
            )
        }
    }


    @Test
    fun constructor_NoCoroutineScopeProvided_SettingsViewModelReturned() {
        val vm = SettingsViewModel(
            get<BackupDatabaseUseCase>(),
            get<RestoreDatabaseUseCase>()
        )

        Assert.assertNotNull(vm)
    }
}
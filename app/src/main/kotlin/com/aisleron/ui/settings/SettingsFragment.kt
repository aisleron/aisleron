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
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.AisleronFragment
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class SettingsFragment : PreferenceFragmentCompat(), AisleronFragment {

    enum class PreferenceOption(val key: String) {
        BACKUP_FOLDER("backup_folder"),
        BACKUP_DATABASE("backup_database"),
        RESTORE_DATABASE("restore_database")
    }

    private val settingsViewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        initializeDbBackupRestorePreference(
            PreferenceOption.BACKUP_FOLDER, this::selectBackupFolder, this::setBackupFolder
        )

        initializeDbBackupRestorePreference(
            PreferenceOption.BACKUP_DATABASE, this::selectBackupFolder, this::backupDatabase
        )

        initializeDbBackupRestorePreference(
            PreferenceOption.RESTORE_DATABASE, this::selectBackupFile, this::restoreDatabase
        )
    }

    private fun initializeDbBackupRestorePreference(
        preferenceOption: PreferenceOption,
        filePicker: (pickerInitialUri: Uri, launcher: ActivityResultLauncher<Intent>) -> Unit,
        onFilePickerResponse: (uri: Uri) -> Unit
    ) {
        val filePickerResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.also { uri -> onFilePickerResponse(uri) }
                }
            }

        findPreference<Preference>(preferenceOption.key)?.let {
            val handler = settingsViewModel.preferenceHandlerFactory(preferenceOption, it)
            handler.setOnPreferenceClickListener {
                val uri = getBackupFolderUri()
                filePicker(uri, filePickerResultLauncher)
                true
            }
        }
    }

    private fun getBackupFolderUri(): Uri {
        val uriStr = settingsViewModel.getPreferenceValue(PreferenceOption.BACKUP_FOLDER)
        return uriStr.toUri()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWindowInsetListeners(this, view, false, null)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState.collect {
                    when (it) {
                        SettingsViewModel.UiState.Empty -> Unit
                        is SettingsViewModel.UiState.Processing -> {
                            it.message?.let { msg ->
                                Snackbar.make(requireView(), msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        is SettingsViewModel.UiState.Error ->
                            displayErrorSnackBar(it.errorCode, it.errorMessage)

                        is SettingsViewModel.UiState.Success -> {
                            Snackbar.make(requireView(), it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun selectBackupFolder(
        initialUri: Uri, launcher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }

        launcher.launch(intent)
    }

    private fun selectBackupFile(
        initialUri: Uri, launcher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/*" // Required to allow multiple types
            putExtra(
                Intent.EXTRA_MIME_TYPES, arrayOf(
                    "application/octet-stream", "application/vnd.sqlite3"
                )
            )


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            }
        }

        launcher.launch(intent)
    }

    private fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }

    private fun setBackupFolder(uri: Uri) {
        settingsViewModel.handleOnPreferenceClick(PreferenceOption.BACKUP_FOLDER, uri)
    }

    private fun backupDatabase(uri: Uri) {
        settingsViewModel.setPreferenceValue(PreferenceOption.BACKUP_FOLDER, uri.toString())
        settingsViewModel.handleOnPreferenceClick(PreferenceOption.BACKUP_DATABASE, uri)
    }

    private fun restoreDatabase(uri: Uri) {
        val filename = requireContext().getFileName(uri)
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder
            .setTitle(getString(R.string.db_restore_confirmation_title))
            .setMessage(getString(R.string.db_restore_confirmation, filename))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                settingsViewModel.handleOnPreferenceClick(PreferenceOption.RESTORE_DATABASE, uri)
            }

        builder.create().show()
    }

    private fun Context.getFileName(uri: Uri): String? = when (uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
                return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                    .let(cursor::getString)
            }
        }

        else -> uri.path?.let(::File)?.name
    }
}
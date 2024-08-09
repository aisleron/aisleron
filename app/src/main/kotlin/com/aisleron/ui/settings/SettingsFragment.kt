package com.aisleron.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R
import com.aisleron.ui.AisleronExceptionMap
import com.aisleron.ui.widgets.ErrorSnackBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val BACKUP_LOCATION = "backup_location"
        const val BACKUP_DATABASE = "backup_database"
        const val RESTORE_DATABASE = "restore_database"
    }

    private lateinit var restoreDbLauncher: ActivityResultLauncher<Intent>
    private lateinit var backupFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var backupDbLauncher: ActivityResultLauncher<Intent>

    private val settingsViewModel: SettingsViewModel by viewModel()


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val backupFolderPreferenceHandler =
            BackupFolderPreferenceHandler(findPreference(BACKUP_LOCATION))
        backupFolderPreferenceHandler.getPreference()?.setOnPreferenceClickListener {
            val uri = backupFolderPreferenceHandler.getValue()
            selectBackupFolder(uri, backupFolderLauncher)
            true
        }

        settingsViewModel.addBackupRestoreDbPreferenceHandler(
            BACKUP_LOCATION, backupFolderPreferenceHandler
        )

        backupFolderLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.also { uri ->
                        settingsViewModel.handleOnPreferenceClick(BACKUP_LOCATION, uri)
                    }
                }
            }

        val backupDbPreferenceHandler = BackupDbPreferenceHandler(findPreference(BACKUP_DATABASE))
        backupDbPreferenceHandler.getPreference()?.setOnPreferenceClickListener {
            val uri = backupFolderPreferenceHandler.getValue()
            selectBackupFolder(uri, backupDbLauncher)
            true
        }

        settingsViewModel.addBackupRestoreDbPreferenceHandler(
            BACKUP_DATABASE, backupDbPreferenceHandler
        )

        backupDbLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.also { uri ->
                        backupFolderPreferenceHandler.setValue(uri.toString())
                        settingsViewModel.handleOnPreferenceClick(BACKUP_DATABASE, uri)
                    }
                }
            }

        val restoreDbPreferenceHandler =
            RestoreDbPreferenceHandler(findPreference(RESTORE_DATABASE))
        restoreDbPreferenceHandler.getPreference()?.setOnPreferenceClickListener {
            val uri = backupFolderPreferenceHandler.getValue()
            selectBackupFile(uri)
            true
        }

        settingsViewModel.addBackupRestoreDbPreferenceHandler(
            RESTORE_DATABASE, restoreDbPreferenceHandler
        )

        restoreDbLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.also { uri ->
                        settingsViewModel.handleOnPreferenceClick(RESTORE_DATABASE, uri)
                    }
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.uiState.collect {
                    when (it) {
                        SettingsViewModel.UiState.Empty -> Unit
                        is SettingsViewModel.UiState.Processing -> {
                            Log.d("Settings", "Processing...")
                            it.message?.let { msg ->
                                Snackbar.make(requireView(), msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        is SettingsViewModel.UiState.Error ->
                            displayErrorSnackBar(it.errorCode, it.errorMessage)

                        is SettingsViewModel.UiState.Success -> {
                            Log.d("Settings", "Completed.")
                            it.message?.let { msg ->
                                Snackbar.make(requireView(), msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun selectBackupFolder(
        pickerInitialUri: String,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(pickerInitialUri))
            }
        }

        launcher.launch(intent)
    }

    private fun selectBackupFile(pickerInitialUri: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            type = "application/octet-stream"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(pickerInitialUri))
            }
        }

        restoreDbLauncher.launch(intent)
    }

    private fun displayErrorSnackBar(errorCode: String, errorMessage: String?) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)
        ErrorSnackBar().make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }
}
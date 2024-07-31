package com.aisleron.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val BACKUP_LOCATION = "backup_location"
        const val BACKUP_DATABASE = "backup_database"
        const val RESTORE_DATABASE = "restore_database"
    }

    private lateinit var restoreDbLauncher: ActivityResultLauncher<Intent>
    private lateinit var backupFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var backupDbLauncher: ActivityResultLauncher<Intent>

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val backupFolderPreferenceHandler =
            BackupFolderPreferenceHandler(findPreference(BACKUP_LOCATION))
        backupFolderPreferenceHandler.getPreference()?.setOnPreferenceClickListener {
            val uri = backupFolderPreferenceHandler.getValue()
            selectBackupFolder(uri, backupFolderLauncher)
            true
        }

        backupFolderLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.also { uri ->
                        backupFolderPreferenceHandler.handleOnPreferenceClick(uri.toString())
                    }
                }
            }

        val backupDbPreferenceHandler = BackupDbPreferenceHandler(findPreference(BACKUP_DATABASE))
        backupDbPreferenceHandler.getPreference()?.setOnPreferenceClickListener {
            val uri = backupFolderPreferenceHandler.getValue()
            selectBackupFolder(uri, backupDbLauncher)
            true
        }

        backupDbLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.also { uri ->
                        backupFolderPreferenceHandler.setValue(uri.toString())
                        backupDbPreferenceHandler.handleOnPreferenceClick(uri.toString())
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

        restoreDbLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.also { uri ->
                        restoreDbPreferenceHandler.handleOnPreferenceClick(uri.toString())
                    }
                }
            }
    }

    private fun selectBackupFolder(
        pickerInitialUri: String,
        launcher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(pickerInitialUri))
            }
        }

        launcher.launch(intent)
    }

    private fun selectBackupFile(pickerInitialUri: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            type = "*/*"
            //type = "application/vnd.sqlite3"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse(pickerInitialUri))
            }
        }

        restoreDbLauncher.launch(intent)
    }

}
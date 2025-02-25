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
import com.aisleron.R

class BackupFolderPreferenceHandler(private val preference: Preference) :
    BackupRestoreDbPreferenceHandler {

    init {
        updateSummary()
    }

    override suspend fun handleOnPreferenceClick(uri: Uri) {
        setValue(uri.toString())
    }

    override fun BackupRestoreDbPreferenceHandler.getPreference() = preference
    override fun getProcessingMessage() = null
    override fun getSuccessMessage(): String {
        return preference.context.getString(R.string.backup_folder_success)
    }
}
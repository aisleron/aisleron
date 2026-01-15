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

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.aisleron.domain.preferences.NoteHint
import com.aisleron.domain.preferences.TrackingMode

class ShoppingListPreferencesImpl(context: Context) : ShoppingListPreferences {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    override fun isStatusChangeSnackBarHidden(): Boolean =
        prefs.getBoolean(PREF_HIDE_STATUS_CHANGE_SNACK_BAR, false)

    override fun showEmptyAisles(): Boolean =
        prefs.getBoolean(PREF_SHOW_EMPTY_AISLES, false)

    override fun setShowEmptyAisles(value: Boolean) {
        prefs.edit {
            putBoolean(PREF_SHOW_EMPTY_AISLES, value)
        }
    }

    override fun trackingMode(): TrackingMode {
        val method = prefs
            .getString(PREF_TRACKING_MODE, TrackingMode.CHECKBOX.value)

        return TrackingMode.fromValue(method)
    }

    override fun keepScreenOn(): Boolean =
        prefs.getBoolean(PREF_KEEP_SCREEN_ON, false)

    override fun noteHint(): NoteHint {
        val noteHint = prefs.getString(
            PREF_NOTE_HINT, NoteHint.NONE.value
        )

        return NoteHint.fromValue(noteHint)
    }

    companion object {
        const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
        const val PREF_SHOW_EMPTY_AISLES = "show_empty_aisles"
        const val PREF_TRACKING_MODE = "tracking_mode"
        const val PREF_KEEP_SCREEN_ON = "keep_screen_on"
        const val PREF_NOTE_HINT = "note_hint"
    }
}
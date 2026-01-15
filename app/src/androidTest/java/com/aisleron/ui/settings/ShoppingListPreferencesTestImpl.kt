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

import com.aisleron.domain.preferences.NoteHint
import com.aisleron.domain.preferences.TrackingMode

class ShoppingListPreferencesTestImpl : ShoppingListPreferences {

    private var _hideStatusChangeSnackBar: Boolean = false
    private var _showEmptyAisles: Boolean = false
    private var _keepScreenOn: Boolean = false
    private var _trackingMode: TrackingMode = TrackingMode.CHECKBOX
    private var _noteHint: NoteHint = NoteHint.NONE

    override fun isStatusChangeSnackBarHidden(): Boolean = _hideStatusChangeSnackBar
    override fun showEmptyAisles(): Boolean = _showEmptyAisles
    override fun keepScreenOn(): Boolean = _keepScreenOn
    override fun noteHint(): NoteHint = _noteHint
    override fun trackingMode(): TrackingMode = _trackingMode

    override fun setShowEmptyAisles(value: Boolean) {
        _showEmptyAisles = value
    }

    fun setHideStatusChangeSnackBar(hideSnackBar: Boolean) {
        _hideStatusChangeSnackBar = hideSnackBar
    }

    fun setTrackingMode(trackingMode: TrackingMode) {
        _trackingMode = trackingMode
    }

    fun setNoteHint(noteHint: NoteHint) {
        _noteHint = noteHint
    }
}
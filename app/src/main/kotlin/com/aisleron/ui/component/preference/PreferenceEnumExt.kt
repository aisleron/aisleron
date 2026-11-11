/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.component.preference

import androidx.annotation.StringRes
import com.aisleron.R
import com.aisleron.domain.preferences.ApplicationTheme
import com.aisleron.domain.preferences.NoteHint
import com.aisleron.domain.preferences.PreferenceEnum
import com.aisleron.domain.preferences.PureBlackStyle
import com.aisleron.domain.preferences.SyncServicePreference
import com.aisleron.domain.preferences.SyncStatusPreference
import com.aisleron.domain.preferences.TrackingMode

@get:StringRes
val PreferenceEnum.labelRes: Int
    get() = when (this) {
        is SyncServicePreference -> when (this) {
            SyncServicePreference.NONE -> R.string.preference_none
            SyncServicePreference.DEFAULT_SERVICE -> R.string.sync_service_default
            SyncServicePreference.CUSTOM_SERVICE -> R.string.sync_service_custom
        }

        is SyncStatusPreference -> when (this) {
            SyncStatusPreference.NONE -> R.string.preference_none
            SyncStatusPreference.SUCCESS -> R.string.sync_status_success
            SyncStatusPreference.RUNNING -> R.string.sync_status_running
            SyncStatusPreference.FAILURE -> R.string.sync_status_failed
            SyncStatusPreference.NOT_SUBSCRIBED -> R.string.sync_status_not_subscribed
        }

        is PureBlackStyle -> when (this) {
            PureBlackStyle.DEFAULT -> R.string.pure_black_default
            PureBlackStyle.ECONOMY -> R.string.pure_black_economy
            PureBlackStyle.BUSINESS_CLASS -> R.string.pure_black_business_class
            PureBlackStyle.FIRST_CLASS -> R.string.pure_black_first_class
        }

        is TrackingMode -> when (this) {
            TrackingMode.CHECKBOX -> R.string.tracking_checkbox
            TrackingMode.QUANTITY -> R.string.tracking_quantity
            TrackingMode.CHECKBOX_QUANTITY -> R.string.tracking_checkbox_quantity
            TrackingMode.NONE -> R.string.tracking_none
            TrackingMode.DEFAULT -> R.string.tracking_default
        }

        is NoteHint -> when (this) {
            NoteHint.BUTTON -> R.string.note_hint_button
            NoteHint.SUMMARY -> R.string.note_hint_summary
            NoteHint.INDICATOR -> R.string.note_hint_indicator
            NoteHint.NONE -> R.string.preference_none
        }

        is ApplicationTheme -> when (this) {
            ApplicationTheme.SYSTEM_THEME -> R.string.system_theme
            ApplicationTheme.LIGHT_THEME -> R.string.light_theme
            ApplicationTheme.DARK_THEME -> R.string.dark_theme
        }
    }

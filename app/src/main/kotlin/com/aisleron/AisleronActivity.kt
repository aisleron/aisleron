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

package com.aisleron

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aisleron.domain.preferences.ApplicationTheme
import com.aisleron.domain.preferences.PureBlackStyle
import com.aisleron.ui.settings.DisplayPreferences
import com.google.android.material.color.DynamicColors

abstract class AisleronActivity : AppCompatActivity() {

    protected fun applyPureBlackStyle(displayPreferences: DisplayPreferences) {
        val pureBlackStyleId = when (displayPreferences.pureBlackStyle()) {
            PureBlackStyle.ECONOMY -> R.style.AisleronPureBlack_Economy
            PureBlackStyle.BUSINESS_CLASS -> R.style.AisleronPureBlack_BusinessClass
            PureBlackStyle.FIRST_CLASS -> R.style.AisleronPureBlack_FirstClass
            else -> 0
        }

        if (pureBlackStyleId == 0) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            theme.applyStyle(pureBlackStyleId, true)
        } else if (displayPreferences.applicationTheme() == ApplicationTheme.DARK_THEME) {
            findViewById<View>(android.R.id.content)?.setBackgroundColor(Color.BLACK)
        }
    }

    protected fun applyDynamicColors(displayPreferences: DisplayPreferences) {
        if (displayPreferences.dynamicColor()) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
    }
}
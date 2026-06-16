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

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.aisleron.ui.about.AboutScreen
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.theme.AisleronTheme


class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))

        super.onCreate(savedInstanceState)

        setContent {
            val displayPreferences = DisplayPreferencesImpl(this)

            AisleronTheme(
                dynamicColor = displayPreferences.dynamicColor(),
                pureBlackStyle = displayPreferences.pureBlackStyle(),
                applicationTheme = displayPreferences.applicationTheme()
            ) {
                AboutScreen(
                    onBackPressed = { finish() },
                    onUrlClick = { url -> onUrlClick(url) }
                )
            }
        }
    }

    private fun onUrlClick(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (_: Exception) {
            // Fail gracefully if no browser app is available
        }
    }
}
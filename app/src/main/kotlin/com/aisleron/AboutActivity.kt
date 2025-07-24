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

package com.aisleron

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.aisleron.databinding.ActivityAboutBinding


class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setWindowInsetListeners()
    }

    private fun setWindowInsetListeners() {
        //TODO: Change system bar style to auto to allow for black status bar text
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(getStatusBarColor()))

        // Need a toolbar shim here as the application toolbar is defined in the main activity.
        val toolbarShim = binding.aboutToolbarShim
        ViewCompat.setOnApplyWindowInsetsListener(toolbarShim) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())

            val params = view.layoutParams
            params.height = insets.top
            view.layoutParams = params

            windowInsets
        }
    }

    private fun getStatusBarColor(): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            resolveThemeColor(android.R.attr.statusBarColor)
        } else {
            Color.TRANSPARENT
        }
    }

    private fun resolveThemeColor(attrResId: Int): Int {
        val typedValue = TypedValue()

        val wasResolved = theme.resolveAttribute(attrResId, typedValue, true)
        if (!wasResolved) {
            throw IllegalArgumentException("Attribute 0x${attrResId.toString(16)} not defined in theme")
        }

        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(this, typedValue.resourceId)
        } else {
            typedValue.data
        }
    }

}
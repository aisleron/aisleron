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

package com.aisleron.ui.widgets

import android.view.View
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar

class ErrorSnackBar {
    fun make(view: View, text: CharSequence, duration: Int, anchorView: View? = null): Snackbar {
        val snackBar = Snackbar.make(view, text, duration)

        val backgroundColor =
            MaterialColors.getColor(view, com.google.android.material.R.attr.colorError)
        val textColor =
            MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnError)
        snackBar.setBackgroundTint(backgroundColor)
        snackBar.setTextColor(textColor)
        snackBar.setAnchorView(anchorView)

        return snackBar
    }
}
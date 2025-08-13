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

package com.aisleron.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.aisleron.R

interface AisleronFragment {
    fun setWindowInsetListeners(
        fragment: Fragment,
        view: View,
        fabPadding: Boolean = true,
        baseMarginId: Int?
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
                        or WindowInsetsCompat.Type.displayCutout()
                        or WindowInsetsCompat.Type.ime()
            )

            var baseMargin = 0
            baseMarginId?.let {
                baseMargin += fragment.resources.getDimensionPixelSize(it)
            }

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = baseMargin + insets.left
                rightMargin = baseMargin + insets.right
                topMargin = baseMargin
                bottomMargin = baseMargin
            }

            var bottomPadding = insets.bottom
            if (fabPadding) {
                val fabMargins =
                    fragment.resources.getDimensionPixelSize(R.dimen.fab_margin_bottom) * 2

                val fabSize =
                    fragment.resources.getDimensionPixelSize(R.dimen.design_fab_size_normal)

                bottomPadding += fabSize + fabMargins
            }

            v.updatePadding(bottom = bottomPadding)

            windowInsets
        }
    }

    /*fun displayErrorSnackBar(
        errorCode: AisleronException.ExceptionCode, errorMessage: String?, anchorView: View?
    ) {
        val snackBarMessage =
            getString(AisleronExceptionMap().getErrorResourceId(errorCode), errorMessage)

        ErrorSnackBar().make(
            requireView(),
            snackBarMessage,
            Snackbar.LENGTH_SHORT,
            anchorView
        ).show()
    }*/
}
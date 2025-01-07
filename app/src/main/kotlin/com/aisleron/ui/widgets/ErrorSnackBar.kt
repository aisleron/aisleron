package com.aisleron.ui.widgets

import android.view.View
import com.aisleron.R
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar

class ErrorSnackBar {
    fun make(view: View, text: CharSequence, duration: Int): Snackbar {
        val snackBar = Snackbar.make(view, text, duration)

        val backgroundColor =
            MaterialColors.getColor(view, com.google.android.material.R.attr.colorError)
        val textColor =
            MaterialColors.getColor(view, com.google.android.material.R.attr.colorOnError)
        snackBar.setBackgroundTint(backgroundColor)
        snackBar.setTextColor(textColor)
        snackBar.setAnchorView(R.id.fab)

        return snackBar
    }
}
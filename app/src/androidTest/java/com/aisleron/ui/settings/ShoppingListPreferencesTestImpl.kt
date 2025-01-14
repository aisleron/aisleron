package com.aisleron.ui.settings

import android.content.Context

class ShoppingListPreferencesTestImpl : ShoppingListPreferences {

    private var _hideStatusChangeSnackBar: Boolean = false

    override fun isStatusChangeSnackBarHidden(context: Context): Boolean = _hideStatusChangeSnackBar

    fun setHideStatusChangeSnackBar(hideSnackBar: Boolean) {
        _hideStatusChangeSnackBar = hideSnackBar
    }
}
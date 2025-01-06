package com.aisleron.ui.settings

class ShoppingListPreferencesTestImpl : ShoppingListPreferences {

    private var _hideStatusChangeSnackBar: Boolean = false

    override val hideStatusChangeSnackBar: Boolean
        get() {
            return _hideStatusChangeSnackBar
        }

    fun setHideStatusChangeSnackBar(hideSnackBar: Boolean) {
        _hideStatusChangeSnackBar = hideSnackBar
    }
}
package com.aisleron.ui.settings

import android.content.SharedPreferences

class ShoppingListPreferencesImpl(private val sharedPreferences: SharedPreferences) :
    ShoppingListPreferences {

    override val hideStatusChangeSnackBar: Boolean
        get() = sharedPreferences.getBoolean(
            PREF_HIDE_STATUS_CHANGE_SNACK_BAR, false
        )

    companion object {
        private const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
    }
}
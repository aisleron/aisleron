package com.aisleron.ui.settings

import android.content.Context
import androidx.preference.PreferenceManager

class ShoppingListPreferencesImpl : ShoppingListPreferences {

    override fun isStatusChangeSnackBarHidden(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            PREF_HIDE_STATUS_CHANGE_SNACK_BAR, false
        )

    companion object {
        private const val PREF_HIDE_STATUS_CHANGE_SNACK_BAR = "hide_status_change_snack_bar"
    }
}
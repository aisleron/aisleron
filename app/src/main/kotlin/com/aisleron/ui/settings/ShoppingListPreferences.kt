package com.aisleron.ui.settings

import android.content.Context

interface ShoppingListPreferences {

    fun isStatusChangeSnackBarHidden(context: Context): Boolean
}
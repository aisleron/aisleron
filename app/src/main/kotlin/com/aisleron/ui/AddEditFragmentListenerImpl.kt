package com.aisleron.ui

import android.app.Activity
import androidx.activity.ComponentActivity

class AddEditFragmentListenerImpl : AddEditFragmentListener {
    override fun addEditActionCompleted(activity: Activity) {
        (activity as ComponentActivity).onBackPressedDispatcher.onBackPressed()
    }
}
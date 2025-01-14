package com.aisleron.ui

import android.app.Activity

class AddEditFragmentListenerTestImpl : AddEditFragmentListener {
    var addEditSuccess: Boolean = false
    override fun addEditActionCompleted(activity: Activity) {
        addEditSuccess = true
    }
}
package com.aisleron.ui

import android.app.Activity

class ApplicationTitleUpdateListenerTestImpl : ApplicationTitleUpdateListener {
    var appTitle: String = ""
    override fun applicationTitleUpdated(activity: Activity, newTitle: String) {
        appTitle = newTitle
    }
}
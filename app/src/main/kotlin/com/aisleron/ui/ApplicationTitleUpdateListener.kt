package com.aisleron.ui

import android.app.Activity

interface ApplicationTitleUpdateListener {
    fun applicationTitleUpdated(activity: Activity, newTitle: String)
}
package com.aisleron.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

class ApplicationTitleUpdateListenerImpl : ApplicationTitleUpdateListener {
    override fun applicationTitleUpdated(activity: Activity, newTitle: String) {
        (activity as AppCompatActivity).supportActionBar?.title = newTitle
    }
}
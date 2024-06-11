package com.aisleron.ui

class TestApplicationTitleUpdateListener : ApplicationTitleUpdateListener {
    var appTitle: String = ""
    override fun applicationTitleUpdated(newTitle: String) {
        appTitle = newTitle
    }
}
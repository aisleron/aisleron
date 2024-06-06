package com.aisleron.ui

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class KoinInstrumentationTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(classLoader, KoinTestApplication::class.java.name, context)
    }
}
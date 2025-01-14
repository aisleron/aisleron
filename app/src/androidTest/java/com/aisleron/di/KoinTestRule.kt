package com.aisleron.di

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.fragment.koin.fragmentFactory
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.module.Module

class KoinTestRule(
    private val modules: List<Module>
) : TestWatcher() {
    override fun starting(description: Description) {
        startKoin {
            fragmentFactory()
            androidContext(InstrumentationRegistry.getInstrumentation().targetContext.applicationContext)
            modules(modules)
        }
    }

    override fun finished(description: Description) {
        stopKoin()
    }
}
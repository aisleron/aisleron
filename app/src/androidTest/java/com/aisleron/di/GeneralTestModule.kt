package com.aisleron.di

import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import org.koin.dsl.module

val generalTestModule = module {
    factory<FabHandler> { FabHandlerTestImpl() }
    factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerTestImpl() }
    factory<AddEditFragmentListener> { AddEditFragmentListenerTestImpl() }
}
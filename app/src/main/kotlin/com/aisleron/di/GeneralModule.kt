package com.aisleron.di

import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AddEditFragmentListenerImpl
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import org.koin.dsl.module

val generalModule = module {
    factory<FabHandler> { FabHandlerImpl() }
    factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
    factory<AddEditFragmentListener> { AddEditFragmentListenerImpl() }
}
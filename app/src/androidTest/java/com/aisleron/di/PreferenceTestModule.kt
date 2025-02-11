package com.aisleron.di

import com.aisleron.ui.settings.DisplayPreferences
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.settings.ShoppingListPreferences
import com.aisleron.ui.settings.ShoppingListPreferencesTestImpl
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.settings.WelcomePreferencesTestImpl
import org.koin.dsl.module

val preferenceTestModule = module {
    factory<DisplayPreferences> { DisplayPreferencesImpl() }
    factory<ShoppingListPreferences> { ShoppingListPreferencesTestImpl() }
    factory<WelcomePreferences> { WelcomePreferencesTestImpl() }
}
package com.aisleron.di

import com.aisleron.ui.settings.DisplayPreferences
import com.aisleron.ui.settings.DisplayPreferencesImpl
import com.aisleron.ui.settings.ShoppingListPreferences
import com.aisleron.ui.settings.ShoppingListPreferencesImpl
import com.aisleron.ui.settings.WelcomePreferences
import com.aisleron.ui.settings.WelcomePreferencesImpl
import org.koin.dsl.module

val preferenceModule = module {
    factory<ShoppingListPreferences> { ShoppingListPreferencesImpl() }
    factory<WelcomePreferences> { WelcomePreferencesImpl() }
    factory<DisplayPreferences> { DisplayPreferencesImpl() }
}
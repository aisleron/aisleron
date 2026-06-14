/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.di

import com.aisleron.data.sync.SupabaseAuthDelegate
import com.aisleron.data.sync.SupabaseAuthDelegateImpl
import com.aisleron.data.sync.SupabaseClientProvider
import com.aisleron.data.sync.SupabaseSessionManagerImpl
import com.aisleron.domain.sync.SyncSessionManager
import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AddEditFragmentListenerImpl
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerImpl
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.loyaltycard.CatimaCardProvider
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.loyaltycard.PackageCheckerImpl
import com.aisleron.ui.navigation.Navigator
import com.aisleron.ui.navigation.NavigatorImpl
import com.aisleron.ui.resourceprovider.ResourceProvider
import com.aisleron.ui.resourceprovider.ResourceProviderImpl
import org.koin.dsl.binds
import org.koin.dsl.module

val generalModule = module {
    single<FabHandler> { FabHandlerImpl(get()) }
    single<ResourceProvider> { ResourceProviderImpl() }
    single<Navigator> { NavigatorImpl(Bundler()) }
    factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerImpl() }
    factory<AddEditFragmentListener> { AddEditFragmentListenerImpl() }
    factory<LoyaltyCardProvider> { CatimaCardProvider(PackageCheckerImpl()) }
    factory<SupabaseAuthDelegate> { SupabaseAuthDelegateImpl() }

    single {
        SupabaseSessionManagerImpl(
            syncPreferences = get(),
            clientFactory = get(),
            authDelegate = get()
        )
    } binds arrayOf(
        SyncSessionManager::class,
        SupabaseClientProvider::class
    )
}
/*
 * Copyright (C) 2025 aisleron.com
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

import com.aisleron.ui.AddEditFragmentListener
import com.aisleron.ui.AddEditFragmentListenerTestImpl
import com.aisleron.ui.ApplicationTitleUpdateListener
import com.aisleron.ui.ApplicationTitleUpdateListenerTestImpl
import com.aisleron.ui.FabHandler
import com.aisleron.ui.FabHandlerTestImpl
import com.aisleron.ui.aisle.AisleDialog
import com.aisleron.ui.aisle.AisleDialogImpl
import com.aisleron.ui.loyaltycard.LoyaltyCardProvider
import com.aisleron.ui.loyaltycard.LoyaltyCardProviderTestImpl
import org.koin.dsl.module

val generalTestModule = module {
    factory<FabHandler> { FabHandlerTestImpl() }
    factory<ApplicationTitleUpdateListener> { ApplicationTitleUpdateListenerTestImpl() }
    factory<AddEditFragmentListener> { AddEditFragmentListenerTestImpl() }
    factory<LoyaltyCardProvider> { LoyaltyCardProviderTestImpl() }
    factory<AisleDialog> { AisleDialogImpl(get()) }
}
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

package com.aisleron.ui.loyaltycard

import android.content.Context
import androidx.fragment.app.Fragment
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

class LoyaltyCardProviderTestImpl() : LoyaltyCardProvider {
    override val packageName: String get() = "Test Loyalty Card Provider"
    override val providerNameStringId: Int
        get() = TODO("Not yet implemented")
    override val providerWebsite: String
        get() = TODO("Not yet implemented")
    override val providerType: LoyaltyCardProviderType get() = LoyaltyCardProviderType.CATIMA

    override fun lookupLoyaltyCardShortcut(context: Context) {

    }

    override fun displayLoyaltyCard(context: Context, loyaltyCard: LoyaltyCard) {

    }

    override fun registerLauncher(
        fragment: Fragment,
        onLoyaltyCardSelected: (LoyaltyCard?) -> Unit
    ) {

    }
}
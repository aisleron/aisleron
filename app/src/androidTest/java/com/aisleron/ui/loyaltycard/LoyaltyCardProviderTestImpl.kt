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
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

class LoyaltyCardProviderTestImpl(
    val loyaltyCardName: String = "Test Loyalty Card",
    val throwNotInstalledException: Boolean = false,
    val throwGenericException: Boolean = false,
    override val packageChecker: PackageChecker = PackageCheckerTestImpl()
) : LoyaltyCardProvider {
    private var _loyaltyCardDisplayed: Boolean = false
    val loyaltyCardDisplayed: Boolean get() = _loyaltyCardDisplayed

    private lateinit var _onLoyaltyCardSelected: (LoyaltyCard?) -> Unit

    override val packageName: String get() = "Test Loyalty Card Provider"
    override val providerNameStringId: Int get() = R.string.app_name
    override val providerWebsite: String get() = "https://aisleron.com"
    override val providerType: LoyaltyCardProviderType get() = LoyaltyCardProviderType.CATIMA

    override fun lookupLoyaltyCardShortcut(context: Context) {
        if (throwNotInstalledException) {
            throw AisleronException.LoyaltyCardProviderException(context.getString(R.string.loyalty_card_provider_missing_exception))
        }

        if (throwGenericException) {
            throw Exception("Something went wrong in the Loyalty Card Provider")
        }

        val loyaltyCard = LoyaltyCard(
            id = 1,
            name = loyaltyCardName,
            provider = LoyaltyCardProviderType.CATIMA,
            intent = " Dummy Intent"
        )

        _onLoyaltyCardSelected(loyaltyCard)
    }

    override fun displayLoyaltyCard(context: Context, loyaltyCard: LoyaltyCard) {
        if (throwNotInstalledException) {
            throw AisleronException.LoyaltyCardProviderException(context.getString(R.string.loyalty_card_provider_missing_exception))
        }

        if (throwGenericException) {
            throw Exception("Something went wrong in the Loyalty Card Provider")
        }

        _loyaltyCardDisplayed = true
    }

    override fun registerLauncher(
        fragment: Fragment, onLoyaltyCardSelected: (LoyaltyCard?) -> Unit
    ) {
        _onLoyaltyCardSelected = onLoyaltyCardSelected
    }
}
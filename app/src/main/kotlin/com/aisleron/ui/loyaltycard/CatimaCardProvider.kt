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

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

class CatimaCardProvider(override val packageChecker: PackageChecker) : LoyaltyCardProvider {
    override val packageName: String get() = "me.hackerchick.catima"
    override val providerNameStringId: Int get() = R.string.loyalty_card_provider_catima
    override val providerWebsite: String get() = "https://catima.app/"

    override val providerType: LoyaltyCardProviderType get() = LoyaltyCardProviderType.CATIMA

    private val lookupActivityClassName = "protect.card_locker.CardShortcutConfigure"
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun lookupLoyaltyCardShortcut(context: Context) {
        if (!isInstalled(context)) {
            throw AisleronException.LoyaltyCardProviderException(context.getString(R.string.loyalty_card_provider_missing_exception))
        }

        val intent = Intent().apply {
            setClassName(packageName, lookupActivityClassName)
        }
        launcher.launch(intent)
    }

    @Suppress("DEPRECATION")
    override fun registerLauncher(
        fragment: Fragment, onLoyaltyCardSelected: (LoyaltyCard?) -> Unit
    ) {
        launcher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultIntent = result.data

                    //TODO: Find alternative to deprecated EXTRA_SHORTCUT_NAME and EXTRA_SHORTCUT_INTENT
                    val cardName = resultIntent?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: ""
                    val shortcutIntent: Intent? = resultIntent?.getParcelableExtra(
                        Intent.EXTRA_SHORTCUT_INTENT
                    )

                    val loyaltyCard: LoyaltyCard? = shortcutIntent?.let {
                        LoyaltyCard(
                            id = 0,
                            name = cardName,
                            provider = providerType,
                            intent = it.toUri(Intent.URI_INTENT_SCHEME)
                        )
                    }

                    onLoyaltyCardSelected(loyaltyCard)
                }
            }
    }
}
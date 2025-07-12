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
import android.content.Intent
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.fragment.app.Fragment
import com.aisleron.R
import com.aisleron.domain.base.AisleronException
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

interface LoyaltyCardProvider {
    val packageName: String
    val providerNameStringId: Int
    val providerWebsite: String
    val providerType: LoyaltyCardProviderType
    val packageChecker: PackageChecker

    fun lookupLoyaltyCardShortcut(context: Context)
    fun registerLauncher(fragment: Fragment, onLoyaltyCardSelected: (LoyaltyCard?) -> Unit)
    fun isInstalled(context: Context): Boolean {
        return packageChecker.isPackageInstalled(context, packageName)
    }

    fun displayLoyaltyCard(context: Context, loyaltyCard: LoyaltyCard) {
        if (!isInstalled(context)) {
            throw AisleronException.LoyaltyCardProviderException(context.getString(R.string.loyalty_card_provider_missing_exception))
        }

        val intent = Intent.parseUri(loyaltyCard.intent, Intent.URI_INTENT_SCHEME)

        context.startActivity(intent)
    }

    fun getNotInstalledDialog(context: Context): AlertDialog {
        val alertTitle = context.getString(R.string.loyalty_card_provider_missing_title)

        val alertMessage = Html.fromHtml(
            context.getString(
                R.string.loyalty_card_provider_missing_message,
                context.getString(providerNameStringId)
            ), FROM_HTML_MODE_LEGACY
        )

        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setTitle(alertTitle)
            .setMessage(alertMessage)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, providerWebsite.toUri()
                )
                context.startActivity(browserIntent)
            }

        return builder.create()
    }
}

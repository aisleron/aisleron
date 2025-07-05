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
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType

class CatimaCardProvider(val context: Context) : LoyaltyCardProvider {
    override val packageName: String get() = "me.hackerchick.catima"
    override val packageManager: PackageManager get() = context.packageManager
    override val providerType: LoyaltyCardProviderType get() = LoyaltyCardProviderType.CATIMA

    private val lookupActivityClassName = "protect.card_locker.CardShortcutConfigure"
    private val displayActivityClassName = "protect.card_locker.LoyaltyCardViewActivity"
    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun lookupLoyaltyCardShortcut() {
        if (!isInstalled()) {
            Toast.makeText(context, "Catima app is not installed.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent().apply {
            setClassName(packageName, lookupActivityClassName)
        }
        launcher.launch(intent)
    }

    override fun displayLoyaltyCard(id: Int) {
        if (!isInstalled()) {
            Toast.makeText(context, "Catima app is not installed.", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent().apply {
            component = ComponentName(packageName, displayActivityClassName)
            putExtra("id", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "App not installed or activity not found.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun registerLauncher(
        fragment: Fragment, onLoyaltyCardSelected: (LoyaltyCard?) -> Unit
    ) {
        launcher =
            fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultIntent = result.data
                    val shortcutIntent =
                        resultIntent?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)

                    val providerCardId = shortcutIntent?.getIntExtra("id", 0)
                    val cardName = resultIntent?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ?: ""
                    val loyaltyCard: LoyaltyCard? = providerCardId?.let {
                        LoyaltyCard(
                            id = 0,
                            name = cardName,
                            providerCardId = it,
                            provider = providerType
                        )
                    }

                    onLoyaltyCardSelected(loyaltyCard)
                } else {
                    Toast.makeText(context, "Shortcut creation canceled", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
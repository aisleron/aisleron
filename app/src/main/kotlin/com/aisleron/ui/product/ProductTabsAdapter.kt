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

package com.aisleron.ui.product

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProductTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    companion object {
        const val PRODUCT_TAB_NOTES = 0
        const val PRODUCT_TAB_AISLES = 1
        const val PRODUCT_TAB_INVENTORY = 2
        const val PRODUCT_TAB_BARCODES = 3
    }

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PRODUCT_TAB_NOTES -> ProductNoteFragment()
            PRODUCT_TAB_AISLES -> ProductAislesFragment()
            else -> throw IllegalArgumentException("Invalid tab index")
        }
    }
}

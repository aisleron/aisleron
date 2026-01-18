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

package com.aisleron.ui.product

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProductTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    enum class ProductTab {
        TAB_NOTES,
        TAB_AISLES,
        TAB_INVENTORY,
        //TAB_BARCODES
    }

    override fun getItemCount(): Int = ProductTab.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            ProductTab.TAB_NOTES.ordinal -> ProductNoteFragment()
            ProductTab.TAB_AISLES.ordinal -> ProductAislesFragment()
            ProductTab.TAB_INVENTORY.ordinal -> ProductInventoryFragment()
            else -> throw IllegalArgumentException("Invalid tab index")
        }
    }
}

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

package com.aisleron.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aisleron.R
import com.aisleron.databinding.FragmentNavigationDrawerBinding
import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.bundles.Bundler
import com.aisleron.ui.shoppinglist.ShoppingListGrouping

class NavigationDrawerFragment : Fragment() {

    private var _binding: FragmentNavigationDrawerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNavigationDrawerBinding.inflate(inflater, container, false)

        with(binding) {
            //Set onclick listener for views that navigate based on their Id matching a navigation graph destination
            val navButtons =
                setOf(navInStock, navNeeded, navAllItems, navSettings, navAllShops, navAbout)

            for (view in navButtons) {
                view.setOnClickListener {
                    findNavController().navigate(it.id, null)
                }
            }

            navNeededInShops.setOnClickListener {
                val bundle = Bundler().makeShoppingListBundle(
                    filterType = FilterType.NEEDED,
                    listGrouping = ShoppingListGrouping.LocationGrouping(LocationType.SHOP)
                )

                findNavController().navigate(R.id.nav_shopping_list, bundle)
            }
        }

        return binding.root
    }
}
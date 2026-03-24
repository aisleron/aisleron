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

package com.aisleron.ui.shopmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.ui.navigation.Navigator
import com.aisleron.ui.shoplist.ShopListItemViewModel
import com.aisleron.ui.shoplist.ShopListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ShopMenuFragment(
    private val navigator: Navigator
) : Fragment(), ShopMenuRecyclerViewAdapter.ShopMenuItemListener {
    private val shopListViewModel: ShopListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shopListViewModel.hydratePinnedShops()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_menu, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    shopListViewModel.shopListUiState.collect {
                        when (it) {
                            is ShopListViewModel.ShopListUiState.Updated -> {
                                (view.adapter as ShopMenuRecyclerViewAdapter).submitList(it.shops)
                            }

                            else -> Unit
                        }
                    }
                }
            }
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = ShopMenuRecyclerViewAdapter(this@ShopMenuFragment)
            }
        }
        return view
    }

    override fun onClick(item: ShopListItemViewModel) {
        navigator.navigateToAisleGroupedProductList(item.id, item.defaultFilter)
    }
}
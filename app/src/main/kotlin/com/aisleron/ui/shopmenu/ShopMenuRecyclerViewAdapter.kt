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

package com.aisleron.ui.shopmenu

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentShopMenuItemBinding
import com.aisleron.domain.location.Location
import com.aisleron.ui.shoplist.ShopListItemDiffCallback
import com.aisleron.ui.shoplist.ShopListItemViewModel

/**
 * [RecyclerView.Adapter] that can display a [Location].
 *
 */
class ShopMenuRecyclerViewAdapter(
    private val listener: ShopMenuItemListener
) : ListAdapter<ShopListItemViewModel, ShopMenuRecyclerViewAdapter.ViewHolder>(
    ShopListItemDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentShopMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            listener.onClick(getItem(position))
        }
    }

    inner class ViewHolder(binding: FragmentShopMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName
    }

    interface ShopMenuItemListener {
        fun onClick(item: ShopListItemViewModel)
    }

}
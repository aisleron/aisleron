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

package com.aisleron.ui.shoplist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentShopListItemBinding

/**
 * [RecyclerView.Adapter] that can display a [ShopListItemViewModel].
 *
 */
class ShopListItemRecyclerViewAdapter(
    private val listener: ShopListItemListener
) : ListAdapter<ShopListItemViewModel, ShopListItemRecyclerViewAdapter.ViewHolder>(
    ShopListItemDiffCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentShopListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            listener.onClick(getItem(position))
        }
        holder.itemView.setOnLongClickListener { view ->
            listener.onLongClick(getItem(position), view)
        }
    }

    inner class ViewHolder(binding: FragmentShopListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName
    }

    interface ShopListItemListener {
        fun onClick(item: ShopListItemViewModel)
        fun onLongClick(item: ShopListItemViewModel, view: View): Boolean
    }

}
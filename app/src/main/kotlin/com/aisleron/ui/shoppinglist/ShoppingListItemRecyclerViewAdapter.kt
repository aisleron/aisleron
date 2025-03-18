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

package com.aisleron.ui.shoppinglist

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding
import java.util.Collections

/**
 * [RecyclerView.Adapter] that can display a [ShoppingListItem].
 *
 */
class ShoppingListItemRecyclerViewAdapter(
    private val listener: ShoppingListItemListener
) : ListAdapter<ShoppingListItem, RecyclerView.ViewHolder>(ShoppingListItemDiffCallback()),
    ShoppingListItemMoveCallbackListener.Listener {

    companion object {
        const val AISLE_VIEW = 1
        const val PRODUCT_VIEW = 2
    }

    private var itemMoved: Boolean = false

    class ShoppingListItemDiffCallback : DiffUtil.ItemCallback<ShoppingListItem>() {
        override fun areItemsTheSame(
            oldItem: ShoppingListItem,
            newItem: ShoppingListItem
        ): Boolean {
            return (oldItem.itemType == newItem.itemType && oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: ShoppingListItem,
            newItem: ShoppingListItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val newViewHolder = when (viewType) {
            AISLE_VIEW -> AisleViewHolder(
                FragmentAisleListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> ProductListItemViewHolder(
                FragmentProductListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        newViewHolder.itemView.setOnLongClickListener { view ->
            listener.onLongClick(getItem(newViewHolder.absoluteAdapterPosition), view)
            true
        }

        newViewHolder.itemView.setOnClickListener {
            listener.onClick(getItem(newViewHolder.absoluteAdapterPosition))
        }

        return newViewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AisleShoppingListItem -> (holder as AisleViewHolder).bind(item)
            is ProductShoppingListItem -> (holder as ProductListItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).itemType) {
            ShoppingListItem.ItemType.AISLE -> AISLE_VIEW
            ShoppingListItem.ItemType.PRODUCT -> PRODUCT_VIEW
        }
    }

    inner class AisleViewHolder(binding: FragmentAisleListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtAisleName
        private val productCountView: TextView = binding.txtProductCnt

        fun bind(item: AisleShoppingListItem) {
            itemView.isLongClickable = true
            contentView.text = item.name
            if (item.isDefault) {
                contentView.setTypeface(null, Typeface.ITALIC)
            } else {
                contentView.setTypeface(null, Typeface.NORMAL)
            }

            productCountView.text = if (item.childCount > 0) item.childCount.toString() else ""
        }
    }

    inner class ProductListItemViewHolder(binding: FragmentProductListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtProductName
        private val inStockView: CheckBox = binding.chkInStock

        fun bind(item: ProductShoppingListItem) {
            itemView.isLongClickable = true
            contentView.text = item.name
            inStockView.isChecked = item.inStock

            inStockView.setOnClickListener { _ ->
                listener.onProductStatusChange(item, inStockView.isChecked)
            }
        }
    }

    interface ShoppingListItemListener {
        fun onClick(item: ShoppingListItem)
        fun onProductStatusChange(item: ProductShoppingListItem, inStock: Boolean)
        fun onListPositionChanged(item: ShoppingListItem, precedingItem: ShoppingListItem?)
        fun onLongClick(item: ShoppingListItem, view: View): Boolean
        fun onMoved(item: ShoppingListItem)
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        itemMoved = true
        val swapList = currentList.toMutableList()
        val item = getItem(fromPosition)
        if (fromPosition < toPosition) {
            for (originalPosition in fromPosition until toPosition) {
                Collections.swap(swapList, originalPosition, originalPosition + 1)
            }
        } else {
            for (originalPosition in fromPosition downTo toPosition + 1) {
                Collections.swap(swapList, originalPosition, originalPosition - 1)
            }
        }
        submitList(swapList)
        listener.onMoved(item)
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder) {}

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        if (!itemMoved || viewHolder.absoluteAdapterPosition < 0) return

        itemMoved = false

        //Collect the aisle details from the row above the moved item; the item above will
        //always be an aisle or in the same aisle as the item was dropped on.
        //Maybe there's a better way to do this.
        val item = getItem(viewHolder.absoluteAdapterPosition)
        var precedingItem: ShoppingListItem? = null
        if (viewHolder.absoluteAdapterPosition > 0) {
            precedingItem = getItem(viewHolder.absoluteAdapterPosition - 1)
        }

        listener.onListPositionChanged(item, precedingItem)
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition) as ProductShoppingListItem
        listener.onProductStatusChange(item, !item.inStock)
    }
}
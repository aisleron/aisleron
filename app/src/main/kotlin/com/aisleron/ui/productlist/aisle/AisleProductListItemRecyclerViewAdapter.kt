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

package com.aisleron.ui.productlist.aisle

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aisleron.R
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentEmptyListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.domain.FilterType
import com.aisleron.domain.preferences.NoteHint
import com.aisleron.domain.preferences.TrackingMode
import com.aisleron.ui.productlist.EmptyListItemViewHolder
import com.aisleron.ui.productlist.ProductListItemListener
import com.aisleron.ui.productlist.ProductListItemViewHolder
import com.aisleron.ui.productlist.ProductShoppingListItem
import com.aisleron.ui.productlist.ShoppingListItem
import com.aisleron.ui.productlist.ShoppingListItemMoveCallbackListener
import java.util.Collections

class AisleProductListItemRecyclerViewAdapter(
    private val listener: AisleProductListItemListener,
    private val defaultTrackingMode: TrackingMode,
    private val defaultUnitOfMeasure: String,
    private val listFilter: FilterType,
    private val noteHint: NoteHint
) : ListAdapter<ShoppingListItem, ViewHolder>(ShoppingListItemDiffCallback()),
    ShoppingListItemMoveCallbackListener.Listener {
    private val longClickHandler: Handler = Handler(Looper.getMainLooper())

    private var longClicked = false
    private var dragStarted = false
    private var itemMoved: Boolean = false

    class ShoppingListItemDiffCallback : DiffUtil.ItemCallback<ShoppingListItem>() {
        override fun areItemsTheSame(
            oldItem: ShoppingListItem, newItem: ShoppingListItem
        ): Boolean {
            return (oldItem.itemType == newItem.itemType && oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: ShoppingListItem, newItem: ShoppingListItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: ShoppingListItem, newItem: ShoppingListItem): Any? {
            val payload = mutableSetOf<String>()

            if (oldItem.selected != newItem.selected) {
                payload.add("SELECTION")
            }

            return payload.ifEmpty { super.getChangePayload(oldItem, newItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewItemType = ShoppingListItem.ItemType.entries[viewType]

        val newViewHolder = when (viewItemType) {
            ShoppingListItem.ItemType.AISLE -> AisleViewHolder(
                FragmentAisleListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            ShoppingListItem.ItemType.PRODUCT -> ProductListItemViewHolder(
                FragmentProductListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ),

                listener, defaultTrackingMode, defaultUnitOfMeasure, noteHint, listFilter
            )

            ShoppingListItem.ItemType.EMPTY_LIST -> EmptyListItemViewHolder(
                FragmentEmptyListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        newViewHolder.itemView.setOnLongClickListener { view ->
            listener.onLongClick(getItem(newViewHolder.absoluteAdapterPosition), view)

            // TODO: Investigate the return value here.
            true
        }

        newViewHolder.itemView.setOnClickListener { view ->
            listener.onClick(getItem(newViewHolder.absoluteAdapterPosition), view)
        }
        newViewHolder.itemView.setOnTouchListener(shoppingListOnTouchListener(newViewHolder))

        return newViewHolder
    }

    private fun shoppingListOnTouchListener(viewHolder: ViewHolder): OnTouchListener {
        return OnTouchListener { v, event ->
            val longPressTimeout = ViewConfiguration.getLongPressTimeout().toLong()
            var result = true

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longClicked = false
                    dragStarted = false
                    longClickHandler.postDelayed({ //long click
                        longClicked = true
                        v.performLongClick()
                    }, longPressTimeout)
                }

                MotionEvent.ACTION_UP -> {
                    longClickHandler.removeCallbacksAndMessages(null)
                    if (!longClicked && !dragStarted) v.performClick()
                }

                MotionEvent.ACTION_MOVE -> {
                    if (longClicked && !dragStarted) {
                        dragStarted = true
                        listener.onDragStart(viewHolder)
                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    longClickHandler.removeCallbacksAndMessages(null)
                }

                else -> result = false
            }

            result
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)

            // Handle each payload
            for (payload in payloads) {
                when (payload) {
                    is Set<*> -> {
                        if (payload.contains("SELECTION")) {
                            holder.itemView.isSelected = item.selected
                        }
                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is AisleShoppingListItem -> (holder as AisleViewHolder).bind(item)
            is ProductShoppingListItem -> (holder as ProductListItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).itemType.ordinal
    }

    inner class AisleViewHolder(binding: FragmentAisleListItemBinding) : ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtAisleName
        private val productCountView: TextView = binding.txtProductCnt
        private val rootView = binding.root

        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: AisleShoppingListItem) {
            rootView.isSelected = item.selected
            contentView.text = item.name
            if (item.isDefault) {
                contentView.setTypeface(null, Typeface.ITALIC)
            } else {
                contentView.setTypeface(null, Typeface.NORMAL)
            }

            contentView.setOnClickListener { _ ->
                if (!listener.hasSelectedItems()) {
                    listener.onAisleExpandToggle(item, !item.expanded)
                } else {
                    itemView.performClick()
                }
            }

            contentView.setOnLongClickListener { _ -> itemView.performLongClick() }
            contentView.setOnTouchListener(shoppingListOnTouchListener(this))
            setExpandedIcon(contentView, item.expanded)

            productCountView.text = if (item.childCount > 0) item.childCount.toString() else ""
        }

        private fun setExpandedIcon(view: TextView, expanded: Boolean) {
            val expandDrawable = when (expanded) {
                true -> R.drawable.baseline_expand_down_24
                false -> R.drawable.baseline_expand_right_24
            }

            view.setCompoundDrawablesRelativeWithIntrinsicBounds(expandDrawable, 0, 0, 0)
        }
    }

    interface AisleProductListItemListener : ProductListItemListener {
        fun onAisleExpandToggle(item: AisleShoppingListItem, expanded: Boolean)
    }

    override fun onRowMove(viewHolder: ViewHolder, target: ViewHolder) {
        listener.onMove(getItem(viewHolder.absoluteAdapterPosition))
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

    override fun onRowSelected(viewHolder: ViewHolder) {}

    override fun onRowClear(viewHolder: ViewHolder) {
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

    override fun onRowSwiped(viewHolder: ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition) as ProductShoppingListItem
        listener.onProductStatusChange(item, !item.inStock)
    }
}
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

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.aisleron.R
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentEmptyListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.domain.FilterType
import com.aisleron.domain.product.TrackingMode
import java.text.DecimalFormat
import java.util.Collections

/**
 * [RecyclerView.Adapter] that can display a [ShoppingListItem].
 *
 */
class ShoppingListItemRecyclerViewAdapter(
    private val listener: ShoppingListItemListener,
    private val defaultTrackingMode: TrackingMode,
    private val defaultUnitOfMeasure: String,
    private val listFilter: FilterType
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
                )
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

    inner class ProductListItemViewHolder(binding: FragmentProductListItemBinding) :
        ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtProductName
        private val inStockView: CheckBox = binding.chkInStock
        private val qtySelector: LinearLayout = binding.stpQtySelector
        private val decQtyButton: ImageButton = binding.btnQtyDec
        private val incQtyButton: ImageButton = binding.btnQtyInc
        private val qtyEdit: EditText = binding.edtQty
        private val rootView = binding.root

        private var qtyWatcher: TextWatcher? = null

        fun bind(item: ProductShoppingListItem) {
            val trackingMode = when (item.trackingMode) {
                TrackingMode.DEFAULT -> defaultTrackingMode
                else -> item.trackingMode
            }

            rootView.isSelected = item.selected

            contentView.text = item.name
            inStockView.isChecked = item.inStock
            inStockView.isVisible = trackingMode in setOf(
                TrackingMode.CHECKBOX,
                TrackingMode.CHECKBOX_QUANTITY
            ) || (listFilter == FilterType.ALL)

            inStockView.setOnClickListener { _ ->
                listener.onProductStatusChange(item, inStockView.isChecked)
            }

            inStockView.setOnLongClickListener { _ -> itemView.performLongClick() }

            // Remove any old watcher
            qtyWatcher?.let {
                qtyEdit.removeTextChangedListener(it)
                qtyWatcher = null
            }

            qtyEdit.setText(formatQty(item.qtyNeeded))
            qtyEdit.setSelection(qtyEdit.text?.length ?: 0)
            qtyEdit.hint = item.unitOfMeasure.ifEmpty { defaultUnitOfMeasure }

            val qtyIncrement = if (item.qtyIncrement > 0) item.qtyIncrement else 1.0

            // Add a new watcher and keep reference
            qtyWatcher = qtyEdit.doAfterTextChanged { editable ->
                val newQty = editable?.toString()?.toDoubleOrNull() ?: 0.0
                listener.onProductQuantityChange(item, newQty)
            }

            qtySelector.isVisible = trackingMode in setOf(
                TrackingMode.QUANTITY,
                TrackingMode.CHECKBOX_QUANTITY
            )

            decQtyButton.setOnClickListener {
                val currentQty = (qtyEdit.text.toString().toDoubleOrNull() ?: 0.0)
                val newQty = maxOf(currentQty - qtyIncrement, 0.0)
                qtyEdit.setText(formatQty(newQty))
            }

            incQtyButton.setOnClickListener {
                val maxLength = qtyEdit.filters
                    .filterIsInstance<InputFilter.LengthFilter>()
                    .firstOrNull()
                    ?.max ?: Int.MAX_VALUE

                val currentQty = (qtyEdit.text.toString().toDoubleOrNull() ?: 0.0)
                val newQty = currentQty + qtyIncrement
                val formattedQty = formatQty(newQty)

                if (formattedQty.length <= maxLength) {
                    qtyEdit.setText(formattedQty)
                }
            }
        }

        private fun formatQty(qty: Double): String =
            if (qty > 0.0)
                DecimalFormat("0.###").format(qty)
            else
                ""
    }

    class EmptyListItemViewHolder(binding: FragmentEmptyListItemBinding) :
        ViewHolder(binding.root)

    interface ShoppingListItemListener {
        fun onClick(item: ShoppingListItem, view: View)
        fun onProductStatusChange(item: ProductShoppingListItem, inStock: Boolean)
        fun onProductQuantityChange(item: ProductShoppingListItem, quantity: Double?)
        fun onListPositionChanged(item: ShoppingListItem, precedingItem: ShoppingListItem?)
        fun onLongClick(item: ShoppingListItem, view: View): Boolean
        fun onMoved(item: ShoppingListItem)
        fun onAisleExpandToggle(item: AisleShoppingListItem, expanded: Boolean)
        fun onDragStart(viewHolder: ViewHolder)
        fun onMove(item: ShoppingListItem)
        fun hasSelectedItems(): Boolean
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
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

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sign

private const val OUT_OF_BOUNDS_SCROLL_MULTIPLIER = 10

class ShoppingListItemMoveCallbackListener(private val adapter: ShoppingListItemRecyclerViewAdapter) :
    ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = when (viewHolder) {
            is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder,
            is ShoppingListItemRecyclerViewAdapter.AisleViewHolder -> ItemTouchHelper.UP or ItemTouchHelper.DOWN

            else -> 0
        }
        val swipeFlags = when (viewHolder) {
            is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            else -> 0
        }
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onRowMove(viewHolder, target)
        return true
    }

    override fun onMoved(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        fromPos: Int,
        target: RecyclerView.ViewHolder,
        toPos: Int,
        x: Int,
        y: Int
    ) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
        adapter.onRowMoved(fromPos, toPos)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder) {
            adapter.onRowSwiped(viewHolder, direction)
        }

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is RecyclerView.ViewHolder) {
                adapter.onRowSelected(viewHolder)
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        adapter.onRowClear(viewHolder)
    }

    override fun canDropOver(
        recyclerView: RecyclerView,
        current: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val allowProductDrop =
            current is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder
                    && !(target is ShoppingListItemRecyclerViewAdapter.AisleViewHolder && target.absoluteAdapterPosition == 0)

        val allowAisleDrop =
            current is ShoppingListItemRecyclerViewAdapter.AisleViewHolder
                    && target is ShoppingListItemRecyclerViewAdapter.AisleViewHolder

        return allowProductDrop || allowAisleDrop
    }

    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {
        //Controls the scroll speed of the recycler view when dragging to the top or bottom edge
        val direction = sign(viewSizeOutOfBounds.toDouble()).toInt()
        return OUT_OF_BOUNDS_SCROLL_MULTIPLIER * direction
    }

    override fun isLongPressDragEnabled(): Boolean = false

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: RecyclerView.ViewHolder)
        fun onRowClear(viewHolder: RecyclerView.ViewHolder)
        fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
        fun onRowMove(viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder)
    }
}
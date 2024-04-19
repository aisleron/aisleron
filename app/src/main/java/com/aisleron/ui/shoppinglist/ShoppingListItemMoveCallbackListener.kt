package com.aisleron.ui.shoppinglist

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ShoppingListItemMoveCallbackListener(private val adapter: ShoppingListItemRecyclerViewAdapter) :
    ItemTouchHelper.Callback() {
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
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
        adapter.onRowMoved(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder) {
            adapter.onRowSwiped(viewHolder, direction)
        }

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder is RecyclerView.ViewHolder) {
                adapter.onRowSelected(viewHolder)
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
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
        return !(target is ShoppingListItemRecyclerViewAdapter.AisleViewHolder
                && current is ShoppingListItemRecyclerViewAdapter.ProductListItemViewHolder
                && target.absoluteAdapterPosition == 0)
    }

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: RecyclerView.ViewHolder)
        fun onRowClear(viewHolder: RecyclerView.ViewHolder)
        fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
    }
}
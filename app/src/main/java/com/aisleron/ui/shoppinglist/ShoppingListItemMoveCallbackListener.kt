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

    interface Listener {
        fun onRowMoved(fromPosition: Int, toPosition: Int)
        fun onRowSelected(viewHolder: RecyclerView.ViewHolder)
        fun onRowClear(viewHolder: RecyclerView.ViewHolder)
        fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
    }
}
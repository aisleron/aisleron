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

    //private var selectedPos = RecyclerView.NO_POSITION
    private var selectedView: View? = null
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
            val old = when (oldItem.itemType) {
                ShoppingListItem.ItemType.AISLE -> oldItem as AisleShoppingListItem
                ShoppingListItem.ItemType.PRODUCT -> oldItem as ProductShoppingListItem
            }

            val new = when (newItem.itemType) {
                ShoppingListItem.ItemType.AISLE -> newItem as AisleShoppingListItem
                ShoppingListItem.ItemType.PRODUCT -> newItem as ProductShoppingListItem
            }

            return old == new
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

        newViewHolder.itemView.setOnLongClickListener { v ->
            //Deselect the previous view, if one exists
            selectedView?.isSelected = false

            //Allocate the current view as the selected view
            selectedView = v
            selectedView?.isSelected = true

            listener.onLongClick(getItem(newViewHolder.absoluteAdapterPosition))
            true
        }

        newViewHolder.itemView.setOnClickListener { v ->
            if (v == selectedView) {
                v.isSelected = false
                selectedView = null
            }
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
        fun onProductStatusChange(item: ShoppingListItem, inStock: Boolean)
        fun onCleared(item: ShoppingListItem)
        fun onLongClick(item: ShoppingListItem): Boolean
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

        val item = getItem(viewHolder.absoluteAdapterPosition)
        val updatedItem = when (item.itemType) {
            ShoppingListItem.ItemType.PRODUCT -> {
                //Collect the aisle details from the row above the moved item; the item above will
                //always be an aisle or in the same aisle as the item was dropped in.
                //Maybe there's a better way to do this.
                val precedingItem = getItem(viewHolder.absoluteAdapterPosition - 1)
                (item as ProductShoppingListItem).copy(
                    aisleId = precedingItem.aisleId,
                    aisleRank = precedingItem.aisleRank,
                    rank = if (precedingItem.itemType == ShoppingListItem.ItemType.PRODUCT) precedingItem.rank + 1 else 1
                )
            }

            ShoppingListItem.ItemType.AISLE -> {
                //Find the max rank of all aisles above the current item in the list
                val aisles = currentList.subList(0, viewHolder.absoluteAdapterPosition)
                    .filter { a -> a.itemType == ShoppingListItem.ItemType.AISLE }
                val newRank = if (aisles.isNotEmpty()) {
                    aisles.maxOf { a -> a.rank } + 1
                } else {
                    1
                }

                (item as AisleShoppingListItem).copy(rank = newRank)
            }
        }
        selectedView?.isSelected = false
        selectedView = null
        itemMoved = false

        listener.onCleared(updatedItem)
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition) as ProductShoppingListItem
        listener.onProductStatusChange(item, !item.inStock)
    }
}
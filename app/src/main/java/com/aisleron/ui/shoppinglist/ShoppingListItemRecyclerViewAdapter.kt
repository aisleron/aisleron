package com.aisleron.ui.shoppinglist

import android.graphics.Typeface
import android.view.LayoutInflater
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
 * [RecyclerView.Adapter] that can display a [ShoppingListItemViewModel].
 *
 */
class ShoppingListItemRecyclerViewAdapter(
    private val listener: ShoppingListItemListener
) : ListAdapter<ShoppingListItemViewModel, RecyclerView.ViewHolder>(ShoppingListItemDiffCallback()),
    ShoppingListItemMoveCallbackListener.Listener {

    companion object {
        const val AISLE_VIEW = 1
        const val PRODUCT_VIEW = 2
    }

    class ShoppingListItemDiffCallback : DiffUtil.ItemCallback<ShoppingListItemViewModel>() {
        override fun areItemsTheSame(
            oldItem: ShoppingListItemViewModel,
            newItem: ShoppingListItemViewModel
        ): Boolean {
            return (oldItem.lineItemType == newItem.lineItemType && oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(
            oldItem: ShoppingListItemViewModel,
            newItem: ShoppingListItemViewModel
        ): Boolean {
            return oldItem == newItem
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == AISLE_VIEW) {
            return AisleViewHolder(
                FragmentAisleListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        return ProductListItemViewHolder(
            FragmentProductListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (item.lineItemType) {
            ShoppingListItemType.AISLE -> (holder as AisleViewHolder).bind(item)
            ShoppingListItemType.PRODUCT -> (holder as ProductListItemViewHolder).bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).lineItemType) {
            ShoppingListItemType.AISLE -> AISLE_VIEW
            ShoppingListItemType.PRODUCT -> PRODUCT_VIEW
        }
    }

    inner class AisleViewHolder(binding: FragmentAisleListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtAisleName
        private val productCountView: TextView = binding.txtProductCnt

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(item: ShoppingListItemViewModel) {
            itemView.isLongClickable = true
            contentView.text = String.format("${item.name} ")
            if (item.inStock) {
                contentView.setTypeface(null, Typeface.ITALIC)
            } else {
                contentView.setTypeface(null, Typeface.NORMAL)
            }

            productCountView.text = if (item.childCount > 0) item.childCount.toString() else ""

            itemView.setOnClickListener {
                listener.onAisleClick(item)
            }
        }
    }

    inner class ProductListItemViewHolder(binding: FragmentProductListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val contentView: TextView = binding.txtProductName
        private val inStockView: CheckBox = binding.chkInStock

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(item: ShoppingListItemViewModel) {
            itemView.isLongClickable = true
            contentView.text = item.name
            inStockView.isChecked = item.inStock
            itemView.setOnClickListener {
                listener.onProductClick(item)
            }

            inStockView.setOnClickListener { _ ->
                listener.onProductStatusChange(item, inStockView.isChecked)
            }
        }
    }

    interface ShoppingListItemListener {
        fun onAisleClick(item: ShoppingListItemViewModel)
        fun onProductClick(item: ShoppingListItemViewModel)
        fun onProductStatusChange(item: ShoppingListItemViewModel, inStock: Boolean)
        fun onProductMoved(updatedList: List<ShoppingListItemViewModel>)
        fun onAisleMoved(updatedList: List<ShoppingListItemViewModel>)

    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        val swapList = currentList.toMutableList()
        println("Moving item from $fromPosition tp $toPosition...")
        if (fromPosition < toPosition) {
            for (originalPosition in fromPosition until toPosition) {
                swapListEntries(swapList, originalPosition, originalPosition + 1)
            }
        } else {
            for (originalPosition in fromPosition downTo toPosition + 1) {
                swapListEntries(swapList, originalPosition, originalPosition - 1)
            }
        }
        submitList(swapList)
    }

    private fun swapListEntries(
        swapList: MutableList<ShoppingListItemViewModel>,
        originalPosition: Int,
        newPosition: Int
    ) {
        Collections.swap(swapList, originalPosition, newPosition)
        updateMovedItemValues(swapList[newPosition], newPosition + 1)
        if (swapList[newPosition].lineItemType == swapList[originalPosition].lineItemType) {
            //Only update rank of swapped item if it is the same type as the moved item
            updateMovedItemValues(swapList[originalPosition], originalPosition + 1)
        }
    }

    private fun updateMovedItemValues(item: ShoppingListItemViewModel, newRank: Int) {
        item.rank = newRank
        item.modified = true
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder) {
        // TODO("Not yet implemented")
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder.absoluteAdapterPosition < 0) return

        val item = getItem(viewHolder.absoluteAdapterPosition)
        when (viewHolder.itemViewType) {
            PRODUCT_VIEW -> {
                //Collect the aisle details from the row above the moved item; the item above will always be
                //in the same aisle as the item was dropped in.
                //Maybe there's a better way to do this.
                item.aisleId = getItem(viewHolder.absoluteAdapterPosition - 1).aisleId
                item.aisleRank = getItem(viewHolder.absoluteAdapterPosition - 1).aisleRank
                listener.onProductMoved(currentList)
            }

            AISLE_VIEW -> listener.onAisleMoved(currentList)
        }
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition)
        listener.onProductStatusChange(item, !item.inStock)
    }
}
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

            itemView.setOnLongClickListener { v ->
                //v.isSelected = true
                listener.onLongClick(item)
            }

            itemView.setOnClickListener {
                listener.onClick(item)
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
                listener.onClick(item)
            }

            itemView.setOnLongClickListener { v ->
                //v.isSelected = true
                listener.onLongClick(item)
            }

            inStockView.setOnClickListener { _ ->
                listener.onProductStatusChange(item, inStockView.isChecked)
            }
        }
    }

    interface ShoppingListItemListener {
        fun onClick(item: ShoppingListItemViewModel)
        fun onProductStatusChange(item: ShoppingListItemViewModel, inStock: Boolean)
        fun onCleared(item: ShoppingListItemViewModel)
        fun onLongClick(item: ShoppingListItemViewModel): Boolean
        fun onMoved(item: ShoppingListItemViewModel)
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
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

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder) {
        // TODO("Not yet implemented")
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder.absoluteAdapterPosition < 0) return

        val item = getItem(viewHolder.absoluteAdapterPosition)
        when (viewHolder.itemViewType) {
            PRODUCT_VIEW -> {
                //Collect the aisle details from the row above the moved item; the item above will
                //always be an aisle or in the same aisle as the item was dropped in.
                //Maybe there's a better way to do this.
                val precedingItem = getItem(viewHolder.absoluteAdapterPosition - 1)
                item.aisleId = precedingItem.aisleId
                item.aisleRank = precedingItem.aisleRank
                item.rank =
                    if (precedingItem.lineItemType == ShoppingListItemType.PRODUCT) precedingItem.rank + 1 else 1
            }

            AISLE_VIEW -> {
                if (viewHolder.absoluteAdapterPosition == 0) {
                    item.rank = 1
                } else {
                    //Find the max rank of all aisles above the current item in the list
                    val aisles = currentList.subList(0, viewHolder.absoluteAdapterPosition)
                        .filter { a -> a.lineItemType == ShoppingListItemType.AISLE }
                    item.rank = aisles.maxOf { a -> a.rank } + 1
                }
            }
        }
        listener.onCleared(item)
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition)
        listener.onProductStatusChange(item, !item.inStock)
    }
}
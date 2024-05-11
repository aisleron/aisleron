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

    //private var selectedPos = RecyclerView.NO_POSITION
    private var selectedView: View? = null
    private var itemMoved: Boolean = false

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

        val newViewHolder = when (viewType) {
            AISLE_VIEW -> AisleViewHolder(
                FragmentAisleListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> ProductListItemViewHolder(
                FragmentProductListItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
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
                //Find the max rank of all aisles above the current item in the list
                val aisles = currentList.subList(0, viewHolder.absoluteAdapterPosition)
                    .filter { a -> a.lineItemType == ShoppingListItemType.AISLE }
                if (aisles.isNotEmpty()) {
                    item.rank = aisles.maxOf { a -> a.rank } + 1
                } else {
                    item.rank = 1
                }
            }
        }
        selectedView?.isSelected = false
        selectedView = null
        itemMoved = false

        listener.onCleared(item)
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = getItem(viewHolder.absoluteAdapterPosition)
        listener.onProductStatusChange(item, !item.inStock)
    }
}
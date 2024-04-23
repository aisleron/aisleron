package com.aisleron.ui.shoppinglist

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding
import java.util.Collections

/**
 * [RecyclerView.Adapter] that can display a [ShoppingListItemViewModel].
 *
 */
class ShoppingListItemRecyclerViewAdapter(
    private val values: List<ShoppingListItemViewModel>,
    private val listener: ShoppingListItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ShoppingListItemMoveCallbackListener.Listener {

    companion object {
        const val AISLE_VIEW = 1
        const val PRODUCT_VIEW = 2
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
        when (values[position].lineItemType) {
            ShoppingListItemType.AISLE -> (holder as AisleViewHolder).bind(values[position])
            ShoppingListItemType.PRODUCT -> (holder as ProductListItemViewHolder).bind(values[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return values[position].id.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return when (values[position].lineItemType) {
            ShoppingListItemType.AISLE -> AISLE_VIEW
            ShoppingListItemType.PRODUCT -> PRODUCT_VIEW
        }
    }

    override fun getItemCount(): Int = values.size

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
            if (item.inStock) contentView.setTypeface(null, Typeface.ITALIC)

            productCountView.text = ""
            // values.count { it.lineItemType == ShoppingListItemType.PRODUCT && it.aisleId == values[absoluteAdapterPosition].aisleId }
            //   .toString()
            // Commented until a refresh can be applied to Aisle after changing product state
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

            inStockView.setOnCheckedChangeListener { _, isChecked ->
                listener.onProductStatusChange(item, isChecked, absoluteAdapterPosition)
            }
        }
    }

    interface ShoppingListItemListener {
        fun onAisleClick(item: ShoppingListItemViewModel)
        fun onProductClick(item: ShoppingListItemViewModel)
        fun onProductStatusChange(
            item: ShoppingListItemViewModel,
            inStock: Boolean,
            absoluteAdapterPosition: Int
        )

        fun onProductMoved(item: ShoppingListItemViewModel)
        fun onAisleMoved(item: ShoppingListItemViewModel)

    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (originalPosition in fromPosition until toPosition) {
                swapListEntries(originalPosition, originalPosition + 1)
            }
        } else {
            for (originalPosition in fromPosition downTo toPosition + 1) {
                swapListEntries(originalPosition, originalPosition - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    private fun swapListEntries(originalPosition: Int, newPosition: Int) {
        Collections.swap(values, originalPosition, newPosition)
        updateMovedItemValues(values[newPosition], newPosition + 1)
        if (values[newPosition].lineItemType == values[originalPosition].lineItemType) {
            //Only update rank of swapped item if it is the same type as the moved item
            updateMovedItemValues(values[originalPosition], originalPosition + 1)
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

        val item = values[viewHolder.absoluteAdapterPosition]

        when (viewHolder.itemViewType) {
            PRODUCT_VIEW -> {
                //Collect the aisle details from the row above the moved item; the item above will always be
                //in the same aisle as the item was dropped in.
                //Maybe there's a better way to do this.
                item.aisleId = values[viewHolder.absoluteAdapterPosition - 1].aisleId
                item.aisleRank = values[viewHolder.absoluteAdapterPosition - 1].aisleRank
                listener.onProductMoved(item)
            }

            AISLE_VIEW -> listener.onAisleMoved(item)
        }
    }

    override fun onRowSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val item = values[viewHolder.absoluteAdapterPosition]
        listener.onProductStatusChange(item, !item.inStock, viewHolder.absoluteAdapterPosition)
    }
}
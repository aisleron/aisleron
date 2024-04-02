package com.aisleron.ui.shoppinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding

/**
 * [RecyclerView.Adapter] that can display a [ShoppingListItemViewModel].
 * TODO: Replace the implementation with code for your data type.
 */
class ShoppingListItemRecyclerViewAdapter(
    private val values: MutableList<ShoppingListItemViewModel>,
    private val listener: ShoppingListItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val AISLE_VIEW = 1
        const val PRODUCT_VIEW = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == AISLE_VIEW) {
            return AisleViewHolder(
                FragmentAisleListItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
            )
        }

        return ProductListItemViewHolder(
            FragmentProductListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (values[position].lineItemType) {
            ShoppingListItemType.AISLE -> (holder as AisleViewHolder).bind(values[position])
            ShoppingListItemType.PRODUCT -> (holder as ProductListItemViewHolder).bind(values[position])
        }
    }

    override fun getItemId(position: Int): Long {
        return values[position].id
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

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(item: ShoppingListItemViewModel) {
            itemView.isLongClickable = true
            contentView.text = String.format("${item.name} (Rank: ${item.aisleRank})")
            itemView.setOnClickListener {
                listener.onAisleClick(item)
            }
        }
    }

    inner class ProductListItemViewHolder(binding: FragmentProductListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val idView: TextView = binding.txtProductId
        private val contentView: TextView = binding.txtProductName
        private val inStockView: CheckBox = binding.chkInStock

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(item: ShoppingListItemViewModel) {
            itemView.isLongClickable = true
            idView.text = item.id.toString()
            contentView.text = item.name
            inStockView.isChecked = item.inStock!!
            itemView.setOnClickListener {
                listener.onProductClick(item)
            }

            inStockView.setOnCheckedChangeListener { _, isChecked ->
                listener.onProductStatusChange(item, isChecked, absoluteAdapterPosition)
            }
        }
    }

    interface ShoppingListItemListener{
        fun onAisleClick(item: ShoppingListItemViewModel)
        fun onProductClick(item: ShoppingListItemViewModel)
        fun onProductStatusChange(
            item: ShoppingListItemViewModel,
            inStock: Boolean,
            absoluteAdapterPosition: Int
        )

    }
}
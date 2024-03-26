package com.aisleron.ui.shoppinglist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.domain.model.Product

/**
 * [RecyclerView.Adapter] that can display a [ShoppingListItemViewModel].
 * TODO: Replace the implementation with code for your data type.
 */
class ShoppingListItemRecyclerViewAdapter(
    private val values: List<ShoppingListItemViewModel>,
    private val listener: ShoppingListItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val AISLE_VIEW = 1
        const val PRODUCT_VIEW = 2
        /*
        fun getObjectType(): ShoppingListItemType{
            return null
        }

         */
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
        return values[position].item.id
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
            contentView.text = String.format("${item.item.name} (Rank: ${item.aisleRank})")
            itemView.setOnClickListener {
                Toast.makeText(
                    contentView.context,
                    "Aisle Click! Id: ${item.item.id}, Name: ${item.item.name}",
                    Toast.LENGTH_SHORT
                ).show()
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
            val product = item.item as Product
            itemView.isLongClickable = true
            setAttributes(product)
            itemView.setOnClickListener {
                Toast.makeText(
                    contentView.context,
                    "Id: ${product.id}, Name: ${product.name}, In Stock: ${product.inStock}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            inStockView.setOnCheckedChangeListener { _, isChecked ->
                product.inStock = isChecked
                setAttributes(product)
            }
        }

        private fun setAttributes(product: Product) {
            idView.text = product.id.toString()
            contentView.text = product.name
            inStockView.isChecked = product.inStock
        }


    }

    interface ShoppingListItemListener
}
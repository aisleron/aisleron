package com.aisleron.ui.productlist

import android.app.PendingIntent.getActivity
import android.view.ContextMenu
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import com.aisleron.R

import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.domain.model.Product

/**
 * [RecyclerView.Adapter] that can display a [Product].
 *
 */
class ProductListItemRecyclerViewAdapter(
    private val values: List<Product>
) : RecyclerView.Adapter<ProductListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentProductListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemId(position: Int): Long {
        return values[position].id.toLong()
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }
    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentProductListItemBinding)
        : RecyclerView.ViewHolder(binding.root)
    {
        private val idView: TextView = binding.txtProductId
        private val contentView: TextView = binding.txtProductName
        private val inStockView: CheckBox = binding.chkInStock

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(product: Product){
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

}
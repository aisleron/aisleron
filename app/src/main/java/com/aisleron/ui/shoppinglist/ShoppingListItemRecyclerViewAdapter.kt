package com.aisleron.ui.shoppinglist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.aisleron.databinding.FragmentShoppingListItemBinding
import com.aisleron.domain.model.Aisle
import com.aisleron.ui.productlist.ProductListItemRecyclerViewAdapter

/**
 * [RecyclerView.Adapter] that can display a [Aisle].
 * TODO: Replace the implementation with code for your data type.
 */
class ShoppingListItemRecyclerViewAdapter(
    private val values: List<Aisle>
) : RecyclerView.Adapter<ShoppingListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentShoppingListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(values[position])
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentShoppingListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val contentView: TextView = binding.txtAisleName
        private val productList: RecyclerView = binding.aisleProductList

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }

        fun bind(aisle: Aisle) {
            contentView.text = String.format("${aisle.name} (Rank: ${aisle.rank})")
            itemView.setOnClickListener {
                Toast.makeText(
                    contentView.context,
                    "Aisle Click! Id: ${aisle.id}, Name: ${aisle.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            productList.layoutManager =
                LinearLayoutManager(itemView.context, LinearLayoutManager.VERTICAL, false)
            productList.adapter = aisle.products?.let { ProductListItemRecyclerViewAdapter(it) }
        }
    }

}
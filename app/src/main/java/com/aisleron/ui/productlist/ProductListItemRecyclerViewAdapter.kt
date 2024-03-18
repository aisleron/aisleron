package com.aisleron.ui.productlist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.aisleron.databinding.FragmentProductListItemBinding
import com.aisleron.model.Product

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id.toString()
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.contentView.context, "Click! Id: ${item.id}, Name: ${item.name}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentProductListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}
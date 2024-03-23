package com.aisleron.ui.shoplist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.aisleron.databinding.FragmentShopListItemBinding
import com.aisleron.model.Location

/**
 * [RecyclerView.Adapter] that can display a [Location].
 * TODO: Replace the implementation with code for your data type.
 */
class ShopListItemRecyclerViewAdapter(
    private val values: List<Location>,
    private val listener: ShopListItemListener
) : RecyclerView.Adapter<ShopListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentShopListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            listener.onItemClick(values[position])
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentShopListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtAisleName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    interface ShopListItemListener {
        fun onItemClick(item: Location)
    }

}
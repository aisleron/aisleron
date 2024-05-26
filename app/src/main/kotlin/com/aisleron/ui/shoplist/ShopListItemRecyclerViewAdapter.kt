package com.aisleron.ui.shoplist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentShopListItemBinding

/**
 * [RecyclerView.Adapter] that can display a [ShopListItemViewModel].
 *
 */
class ShopListItemRecyclerViewAdapter(
    private val values: List<ShopListItemViewModel>,
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
            listener.onClick(values[position])
        }
        holder.itemView.setOnLongClickListener {
            listener.onLongClick(values[position])
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentShopListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    interface ShopListItemListener {
        fun onClick(item: ShopListItemViewModel)
        fun onLongClick(item: ShopListItemViewModel): Boolean

    }

}
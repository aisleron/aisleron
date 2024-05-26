package com.aisleron.ui.shopmenu

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.aisleron.databinding.FragmentShopMenuItemBinding
import com.aisleron.domain.location.Location
import com.aisleron.ui.shoplist.ShopListItemViewModel

/**
 * [RecyclerView.Adapter] that can display a [Location].
 *
 */
class ShopMenuRecyclerViewAdapter(
    private val values: List<ShopListItemViewModel>,
    private val listener: ShopMenuItemListener
) : RecyclerView.Adapter<ShopMenuRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentShopMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
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

    inner class ViewHolder(binding: FragmentShopMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    interface ShopMenuItemListener {
        fun onItemClick(item: ShopListItemViewModel)
    }

}
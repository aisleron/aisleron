package com.aisleron.ui.shoplist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentShopListItemBinding

/**
 * [RecyclerView.Adapter] that can display a [ShopListItemViewModel].
 *
 */
class ShopListItemRecyclerViewAdapter(
    private val listener: ShopListItemListener
) : ListAdapter<ShopListItemViewModel, ShopListItemRecyclerViewAdapter.ViewHolder>(
    ShopListItemDiffCallback()
) {
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
        val item = getItem(position)
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            listener.onClick(getItem(position))
        }
        holder.itemView.setOnLongClickListener {
            listener.onLongClick(getItem(position))
        }
    }

    inner class ViewHolder(binding: FragmentShopListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName
    }

    interface ShopListItemListener {
        fun onClick(item: ShopListItemViewModel)
        fun onLongClick(item: ShopListItemViewModel): Boolean

    }

}
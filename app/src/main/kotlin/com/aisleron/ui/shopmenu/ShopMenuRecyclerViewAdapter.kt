package com.aisleron.ui.shopmenu

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.databinding.FragmentShopMenuItemBinding
import com.aisleron.domain.location.Location
import com.aisleron.ui.shoplist.ShopListItemDiffCallback
import com.aisleron.ui.shoplist.ShopListItemViewModel

/**
 * [RecyclerView.Adapter] that can display a [Location].
 *
 */
class ShopMenuRecyclerViewAdapter(
    private val listener: ShopMenuItemListener
) : ListAdapter<ShopListItemViewModel, ShopMenuRecyclerViewAdapter.ViewHolder>(
    ShopListItemDiffCallback()
) {
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
        val item = getItem(position)
        holder.contentView.text = item.name
        holder.itemView.setOnClickListener {
            listener.onClick(getItem(position))
        }
    }

    inner class ViewHolder(binding: FragmentShopMenuItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtShopName
    }

    interface ShopMenuItemListener {
        fun onClick(item: ShopListItemViewModel)
    }

}
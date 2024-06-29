package com.aisleron.ui.shoplist

import androidx.recyclerview.widget.DiffUtil

class ShopListItemDiffCallback : DiffUtil.ItemCallback<ShopListItemViewModel>() {
    override fun areItemsTheSame(
        oldItem: ShopListItemViewModel,
        newItem: ShopListItemViewModel
    ): Boolean {
        return (oldItem.id == newItem.id)
    }

    override fun areContentsTheSame(
        oldItem: ShopListItemViewModel,
        newItem: ShopListItemViewModel
    ): Boolean {
        return oldItem == newItem
    }
}
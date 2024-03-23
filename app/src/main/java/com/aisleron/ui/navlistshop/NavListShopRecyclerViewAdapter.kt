package com.aisleron.ui.navlistshop

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.aisleron.databinding.FragmentNavListShopBinding
import com.aisleron.domain.model.Location

/**
 * [RecyclerView.Adapter] that can display a [Location].
 * TODO: Replace the implementation with code for your data type.
 */
class NavListShopRecyclerViewAdapter(
    private val values: List<Location>,
    private val listener: NavListShopItemListener
) : RecyclerView.Adapter<NavListShopRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentNavListShopBinding.inflate(
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

    inner class ViewHolder(binding: FragmentNavListShopBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtAisleName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    interface NavListShopItemListener {
        fun onItemClick(item: Location)
    }

}
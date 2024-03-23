package com.aisleron.ui.aislelist

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.aisleron.databinding.FragmentAisleListItemBinding
import com.aisleron.domain.model.Aisle
import com.aisleron.ui.productlist.ProductListItemRecyclerViewAdapter

/**
 * [RecyclerView.Adapter] that can display a [Aisle].
 * TODO: Replace the implementation with code for your data type.
 */
class MyAisleListItemRecyclerViewAdapter(
    private val values: List<Aisle>
) : RecyclerView.Adapter<MyAisleListItemRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentAisleListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.contentView.text = "${item.name} (Rank: ${item.rank})"
        holder.itemView.setOnClickListener {
            Toast.makeText(holder.contentView.context, "Aisle Click! Id: ${item.id}, Name: ${item.name}", Toast.LENGTH_SHORT).show()
        }
        val plirAdapter = item.products?.let { ProductListItemRecyclerViewAdapter(it) }

        holder.productList.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.VERTICAL,false)
        holder.productList.adapter = plirAdapter
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentAisleListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.txtAisleName
        val productList: RecyclerView = binding.aisleProductList



        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}
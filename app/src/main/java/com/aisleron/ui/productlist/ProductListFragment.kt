package com.aisleron.ui.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aisleron.R
import com.aisleron.domain.FilterType
import com.aisleron.placeholder.ProductData

/**
 * A fragment representing a list of Items.
 */
class ProductListFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_list, container, false)
        val bundle = arguments
        val locationId: Int = bundle?.getInt(ARG_LOCATION_ID) ?: 1
        val filterType: FilterType =
            if (bundle != null) bundle.get(ARG_FILTER_TYPE) as FilterType else FilterType.ALL
        val locationTitle: String? = bundle?.getString("locationTitle")
        if (locationTitle != null) {
            (activity as AppCompatActivity).supportActionBar?.title = locationTitle
        }

        // Set the adapter
        if (view is RecyclerView) {
            view.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            val callback = object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN, //bitwise OR
                ItemTouchHelper.START or ItemTouchHelper.END //bitwise OR
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = viewHolder.absoluteAdapterPosition
                    val toPosition = target.absoluteAdapterPosition

                    //modifying the dataset as well is optional for this tutorial
                    //                val movedItem = dataset.removeAt(fromPosition)
                    //                dataset.add(toPosition, movedItem)

                    //push specific event
                    recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.absoluteAdapterPosition

                    //Actually removes the item from the dataset
                    //dataset.removeAt(position)

                    //push specific event
                    view.adapter?.notifyItemRemoved(position)
                }

            }

            with(view) {

                //Creates touch helper with callback
                val touchHelper = ItemTouchHelper(callback)

                //attaches the helper to the recyclerView
                touchHelper.attachToRecyclerView(this)

                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }

                adapter = ProductListItemRecyclerViewAdapter(ProductData.products.filter { p ->
                    (p.inStock && filterType == FilterType.IN_STOCK)
                            || (!p.inStock && filterType == FilterType.NEEDED)
                            || (filterType == FilterType.ALL)
                })
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_LOCATION_ID = "locationId"
        const val ARG_FILTER_TYPE = "filterType"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, locationId: Long, filterType: FilterType) =
            ProductListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    putInt(ARG_LOCATION_ID, locationId.toInt())
                    putSerializable(ARG_FILTER_TYPE, filterType)
                }
            }
    }
}
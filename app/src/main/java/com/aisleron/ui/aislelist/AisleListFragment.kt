package com.aisleron.ui.aislelist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.aisleron.R
import com.aisleron.domain.model.Aisle
import com.aisleron.domain.model.FilterType
import com.aisleron.placeholder.LocationData

/**
 * A fragment representing a list of Items.
 */
class AisleListFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_aisle_list, container, false)
        val bundle = arguments
        val locationId : Int = bundle?.getInt("locationId") ?: 1
        val filterType : FilterType = if (bundle != null) bundle.get("filterType") as FilterType else FilterType.ALL
        val locationTitle : String? = bundle?.getString("locationTitle")
        if (locationTitle != null) {
            (activity as AppCompatActivity).supportActionBar?.title = locationTitle
        }

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = (LocationData.locations.filter{ l -> l.id == locationId })[0].aisles?.let {
                    MyAisleListItemRecyclerViewAdapter(
                        it.sortedWith(compareBy(Aisle::rank, Aisle::name))
                        //it.sortedWith(compareBy<Aisle> { a -> a.rank}.thenBy { a-> a.name } )
                    )
                }
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            AisleListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
package com.aisleron.ui

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.aisleron.R
import com.aisleron.ui.bundles.Bundler
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabHandlerImpl(private val activity: Activity) : FabHandler {

    private val fabMain = activity.findViewById<FloatingActionButton>(R.id.fab)

    private val fabAddProduct = activity.findViewById<FloatingActionButton>(R.id.fab_add_product)
    private val lblAddProduct = activity.findViewById<TextView>(R.id.add_product_fab_label)

    private val fabAddAisle = activity.findViewById<FloatingActionButton>(R.id.fab_add_aisle)
    private val lblAddAisle = activity.findViewById<TextView>(R.id.add_aisle_fab_label)

    private val fabAddShop = activity.findViewById<FloatingActionButton>(R.id.fab_add_shop)
    private val lblAddShop = activity.findViewById<TextView>(R.id.add_shop_fab_label)

    private var fabEntries = mutableListOf<FabHandler.FabOption>()

    override var allFabAreHidden: Boolean = true

    private fun hideSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.hide()
        label.visibility = View.GONE
    }

    private fun showSingleFabViews(fab: FloatingActionButton, label: TextView) {
        fab.show()
        label.visibility = View.VISIBLE
    }

    private fun hideAllFab() {
        for (fabOption in FabHandler.FabOption.entries) {
            when (fabOption) {
                FabHandler.FabOption.ADD_PRODUCT -> hideSingleFabViews(fabAddProduct, lblAddProduct)
                FabHandler.FabOption.ADD_AISLE -> hideSingleFabViews(fabAddAisle, lblAddAisle)
                FabHandler.FabOption.ADD_SHOP -> hideSingleFabViews(fabAddShop, lblAddShop)
            }
        }

        allFabAreHidden = true
    }

    private fun showAllFab() {
        for (fabOption in fabEntries) {
            when (fabOption) {
                FabHandler.FabOption.ADD_PRODUCT -> showSingleFabViews(fabAddProduct, lblAddProduct)
                FabHandler.FabOption.ADD_AISLE -> showSingleFabViews(fabAddAisle, lblAddAisle)
                FabHandler.FabOption.ADD_SHOP -> showSingleFabViews(fabAddShop, lblAddShop)
            }
        }

        allFabAreHidden = false
    }

    override fun setFabOnClickListener(
        fabOption: FabHandler.FabOption,
        onClickListener: View.OnClickListener
    ) {
        val fab = getFabFromOption(fabOption)

        fab.setOnClickListener {
            onClickListener.onClick(it)
            hideAllFab()
        }
    }

    private fun getFabFromOption(fabOption: FabHandler.FabOption): FloatingActionButton =
        when (fabOption) {
            FabHandler.FabOption.ADD_PRODUCT -> fabAddProduct
            FabHandler.FabOption.ADD_AISLE -> fabAddAisle
            FabHandler.FabOption.ADD_SHOP -> fabAddShop
        }

    private fun setMainFabToSingleOption() {
        getFabFromOption(fabEntries.first()).let {
            fabMain.setImageDrawable(it.drawable)
            fabMain.setOnClickListener { _ -> it.callOnClick() }
        }

        fabMain.show()
    }

    private fun setMainFabToMultiOption() {
        fabMain.setImageDrawable(
            ResourcesCompat.getDrawable(
                activity.resources, android.R.drawable.ic_input_add, activity.theme
            )
        )

        fabMain.setOnClickListener {
            if (allFabAreHidden) {
                showAllFab()
            } else {
                hideAllFab()
            }
        }

        fabMain.show()
    }

    override fun setFabItems(vararg fabOptions: FabHandler.FabOption) {
        fabEntries = fabOptions.distinctBy { it.name }.toMutableList()
        when (fabEntries.count()) {
            0 -> fabMain.hide()
            1 -> setMainFabToSingleOption()
            else -> setMainFabToMultiOption()
        }

        hideAllFab()
        setFabOnClickListener(FabHandler.FabOption.ADD_SHOP) {
            val bundle = Bundler().makeAddLocationBundle()
            activity.findNavController(R.id.nav_host_fragment_content_main)
                .navigate(R.id.nav_add_shop, bundle)
        }
    }


}
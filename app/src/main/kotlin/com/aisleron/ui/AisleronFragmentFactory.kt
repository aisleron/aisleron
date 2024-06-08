package com.aisleron.ui

import androidx.core.view.MenuHost
import androidx.fragment.app.FragmentFactory
import com.aisleron.ui.product.ProductFragment
import com.aisleron.ui.shop.ShopFragment
import com.aisleron.ui.shoppinglist.ShoppingListFragment

class AisleronFragmentFactory(
    private val addEditFragmentListener: AddEditFragmentListener,
    private val menuHost: MenuHost
) : FragmentFactory() {
    override fun instantiate(
        classLoader: ClassLoader,
        className: String
    ) = when (className) {
        ShopFragment::class.java.name -> ShopFragment(addEditFragmentListener, menuHost)
        ProductFragment::class.java.name -> ProductFragment(addEditFragmentListener, menuHost)
        ShoppingListFragment::class.java.name -> ShoppingListFragment(addEditFragmentListener)
        else -> super.instantiate(classLoader, className)
    }
}
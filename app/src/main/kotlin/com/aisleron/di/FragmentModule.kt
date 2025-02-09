package com.aisleron.di

import com.aisleron.ui.product.ProductFragment
import com.aisleron.ui.shop.ShopFragment
import com.aisleron.ui.shoplist.ShopListFragment
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import com.aisleron.ui.welcome.WelcomeFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.dsl.module

val fragmentModule = module {
    includes(generalModule, preferenceModule)

    fragment {
        ShoppingListFragment(
            applicationTitleUpdateListener = get(),
            fabHandler = get(),
            shoppingListPreferences = get()
        )
    }

    fragment {
        ProductFragment(
            addEditFragmentListener = get(),
            applicationTitleUpdateListener = get(),
            fabHandler = get()
        )
    }

    fragment {
        ShopFragment(
            addEditFragmentListener = get(),
            applicationTitleUpdateListener = get(),
            fabHandler = get()
        )
    }

    fragment { ShopListFragment(fabHandler = get()) }

    fragment {
        WelcomeFragment(
            fabHandler = get(),
            welcomePreferences = get(),
            addEditFragmentListener = get()
        )
    }

}
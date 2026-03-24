/*
 * Copyright (C) 2025-2026 aisleron.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aisleron.di

import com.aisleron.ui.menu.NavigationDrawerFragment
import com.aisleron.ui.product.ProductFragment
import com.aisleron.ui.shop.ShopFragment
import com.aisleron.ui.shoplist.ShopListFragment
import com.aisleron.ui.shopmenu.ShopMenuFragment
import com.aisleron.ui.shoppinglist.ShoppingListFragment
import com.aisleron.ui.welcome.WelcomeFragment
import org.koin.androidx.fragment.dsl.fragment
import org.koin.dsl.module

val fragmentModule = module {
    fragment {
        ShoppingListFragment(
            applicationTitleUpdateListener = get(),
            fabHandler = get(),
            shoppingListPreferences = get(),
            loyaltyCardProvider = get(),
            navigator = get()
        )
    }

    fragment {
        ProductFragment(
            addEditFragmentListener = get(),
            applicationTitleUpdateListener = get(),
            productPreferences = get(),
            fabHandler = get(),
            navigator = get()
        )
    }

    fragment {
        ShopFragment(
            addEditFragmentListener = get(),
            applicationTitleUpdateListener = get(),
            loyaltyCardProvider = get(),
            shopPreferences = get()
        )
    }

    fragment {
        ShopListFragment(
            fabHandler = get(),
            navigator = get()
        )
    }

    fragment {
        ShopMenuFragment(
            navigator = get()
        )
    }

    fragment {
        NavigationDrawerFragment(
            navigator = get()
        )
    }

    fragment {
        WelcomeFragment(
            welcomePreferences = get(),
            navigator = get()
        )
    }

}
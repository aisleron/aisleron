/*
 * Copyright (C) 2025 aisleron.com
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

package com.aisleron.ui.settings

import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShoppingListPreferencesImplTest {

    @Test
    fun getSnackBarHidden_isHidden_ReturnTrue() {
        SharedPreferencesInitializer().setHideStatusChangeSnackBar(true)
        val isStatusChangeSnackBarHidden =
            ShoppingListPreferencesImpl().isStatusChangeSnackBarHidden(getInstrumentation().targetContext)

        assertTrue(isStatusChangeSnackBarHidden)
    }

    @Test
    fun getSnackBarHidden_isNotHidden_ReturnFalse() {
        SharedPreferencesInitializer().setHideStatusChangeSnackBar(false)
        val isStatusChangeSnackBarHidden =
            ShoppingListPreferencesImpl().isStatusChangeSnackBarHidden(getInstrumentation().targetContext)

        assertFalse(isStatusChangeSnackBarHidden)
    }

    @Test
    fun getShowEmptyAisles_isShown_ReturnTrue() {
        SharedPreferencesInitializer().setShowEmptyAisles(true)
        val showEmptyAisles =
            ShoppingListPreferencesImpl().showEmptyAisles(getInstrumentation().targetContext)

        assertTrue(showEmptyAisles)
    }

    @Test
    fun getShowEmptyAisles_isNotShown_ReturnFalse() {
        SharedPreferencesInitializer().setShowEmptyAisles(false)
        val showEmptyAisles =
            ShoppingListPreferencesImpl().showEmptyAisles(getInstrumentation().targetContext)

        assertFalse(showEmptyAisles)
    }

    private fun setShowEmptyAisles_ArrangeAct(showEmptyAisles: Boolean): Boolean {
        SharedPreferencesInitializer().setShowEmptyAisles(!showEmptyAisles)
        val context = getInstrumentation().targetContext

        ShoppingListPreferencesImpl().setShowEmptyAisles(
            getInstrumentation().targetContext, showEmptyAisles
        )

        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        return preference.getBoolean("show_empty_aisles", !showEmptyAisles)
    }

    @Test
    fun setShowEmptyAisles_ValueTrue_PreferenceValueIsTrue() {
        val showEmptyAisles = setShowEmptyAisles_ArrangeAct(true)

        assertTrue(showEmptyAisles)
    }

    @Test
    fun setShowEmptyAisles_ValueFalse_PreferenceValueIsFalse() {
        val showEmptyAisles = setShowEmptyAisles_ArrangeAct(false)

        assertFalse(showEmptyAisles)
    }

    private fun getStockMethod_ArrangeAct(stockMethod: SharedPreferencesInitializer.StockMethod): ShoppingListPreferences.StockMethod {
        SharedPreferencesInitializer().setStockMethod(stockMethod)
        return ShoppingListPreferencesImpl().stockMethod(getInstrumentation().targetContext)
    }

    @Test
    fun getStockMethod_isCheckbox_ReturnCheckbox() {
        val stockMethod =
            getStockMethod_ArrangeAct(SharedPreferencesInitializer.StockMethod.CHECKBOX)

        assertEquals(ShoppingListPreferences.StockMethod.CHECKBOX, stockMethod)
    }

    @Test
    fun getStockMethod_isQuantities_ReturnQuantities() {
        val stockMethod =
            getStockMethod_ArrangeAct(SharedPreferencesInitializer.StockMethod.QUANTITIES)

        assertEquals(ShoppingListPreferences.StockMethod.QUANTITIES, stockMethod)
    }

    @Test
    fun getStockMethod_isCheckboxAndQuantities_ReturnCheckboxAndQuantities() {
        val stockMethod =
            getStockMethod_ArrangeAct(SharedPreferencesInitializer.StockMethod.CHECKBOX_QUANTITIES)

        assertEquals(ShoppingListPreferences.StockMethod.CHECKBOX_QUANTITIES, stockMethod)
    }

    @Test
    fun getStockMethod_isNone_ReturnNone() {
        val stockMethod = getStockMethod_ArrangeAct(SharedPreferencesInitializer.StockMethod.NONE)
        assertEquals(ShoppingListPreferences.StockMethod.NONE, stockMethod)
    }
}
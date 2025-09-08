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

    private fun getTrackingMode_ArrangeAct(trackingMode: SharedPreferencesInitializer.TrackingMode): ShoppingListPreferences.TrackingMode {
        SharedPreferencesInitializer().setTrackingMode(trackingMode)
        return ShoppingListPreferencesImpl().trackingMode(getInstrumentation().targetContext)
    }

    @Test
    fun getTrackingMode_isCheckbox_ReturnCheckbox() {
        val trackingMode =
            getTrackingMode_ArrangeAct(SharedPreferencesInitializer.TrackingMode.CHECKBOX)

        assertEquals(ShoppingListPreferences.TrackingMode.CHECKBOX, trackingMode)
    }

    @Test
    fun getTrackingMode_isQuantity_ReturnQuantity() {
        val trackingMode =
            getTrackingMode_ArrangeAct(SharedPreferencesInitializer.TrackingMode.QUANTITY)

        assertEquals(ShoppingListPreferences.TrackingMode.QUANTITY, trackingMode)
    }

    @Test
    fun getTrackingMode_isCheckboxAndQuantity_ReturnCheckboxAndQuantity() {
        val trackingMode =
            getTrackingMode_ArrangeAct(SharedPreferencesInitializer.TrackingMode.CHECKBOX_QUANTITY)

        assertEquals(ShoppingListPreferences.TrackingMode.CHECKBOX_QUANTITY, trackingMode)
    }

    @Test
    fun getTrackingMode_isNone_ReturnNone() {
        val trackingMode = getTrackingMode_ArrangeAct(SharedPreferencesInitializer.TrackingMode.NONE)
        assertEquals(ShoppingListPreferences.TrackingMode.NONE, trackingMode)
    }

    @Test
    fun getKeepScreenOn_isSet_ReturnTrue() {
        SharedPreferencesInitializer().setKeepScreenOn(true)
        val showEmptyAisles =
            ShoppingListPreferencesImpl().keepScreenOn(getInstrumentation().targetContext)

        assertTrue(showEmptyAisles)
    }

    @Test
    fun getKeepScreenOn_isNotShown_ReturnFalse() {
        SharedPreferencesInitializer().setKeepScreenOn(false)
        val showEmptyAisles =
            ShoppingListPreferencesImpl().keepScreenOn(getInstrumentation().targetContext)

        assertFalse(showEmptyAisles)
    }
}
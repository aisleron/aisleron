/*
 * Copyright (C) 2026 aisleron.com
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

package com.aisleron.ui.bundles

import com.aisleron.domain.FilterType
import com.aisleron.domain.location.LocationType
import com.aisleron.ui.shoppinglist.ShoppingListGrouping
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShoppingListBundleTest {
    @Test
    fun invoke_HasLocationId_ReturnsAisleGrouping() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK
        val locationType = null

        val bundle = ShoppingListBundle(locationId, filterType, locationType)

        assertTrue(bundle.listGrouping is ShoppingListGrouping.AisleGrouping)
        assertEquals(locationId, bundle.listGrouping.locationId)
    }

    @Test
    fun invoke_HasLocationType_ReturnsLocationGrouping() {
        val locationId = null
        val filterType = FilterType.NEEDED
        val locationType = LocationType.SHOP

        val bundle = ShoppingListBundle(locationId, filterType, locationType)

        assertTrue(bundle.listGrouping is ShoppingListGrouping.LocationGrouping)
        assertEquals(locationType, bundle.listGrouping.locationType)
    }

    @Test
    fun invoke_HasNoLocationIdOrLocationType_ReturnsAisleGrouping() {
        val locationId = null
        val filterType = FilterType.ALL
        val locationType = null

        val bundle = ShoppingListBundle(locationId, filterType, locationType)

        assertTrue(bundle.listGrouping is ShoppingListGrouping.AisleGrouping)
        assertEquals(1, bundle.listGrouping.locationId)
    }

    @Test
    fun invoke_HasLocationIdAndLocationType_ReturnsAisleGrouping() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK
        val locationType = LocationType.SHOP

        val bundle = ShoppingListBundle(locationId, filterType, locationType)

        assertTrue(bundle.listGrouping is ShoppingListGrouping.AisleGrouping)
        assertEquals(locationId, bundle.listGrouping.locationId)
    }

    @Test
    fun onCreate_WithAisleGrouping_ReturnsAisleGrouping() {
        val locationId = 1
        val filterType = FilterType.IN_STOCK

        val bundle = ShoppingListBundle(filterType, ShoppingListGrouping.AisleGrouping(locationId))

        assertTrue(bundle.listGrouping is ShoppingListGrouping.AisleGrouping)
        assertEquals(locationId, bundle.listGrouping.locationId)
    }

    @Test
    fun onCreate_WithLocationGrouping_ReturnsLocationGrouping() {
        val filterType = FilterType.IN_STOCK
        val locationType = LocationType.SHOP

        val bundle =
            ShoppingListBundle(filterType, ShoppingListGrouping.LocationGrouping(locationType))

        assertTrue(bundle.listGrouping is ShoppingListGrouping.LocationGrouping)
        assertEquals(locationType, bundle.listGrouping.locationType)
    }

}
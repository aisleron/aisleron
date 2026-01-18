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

package com.aisleron.ui.loyaltycard

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class PackageCheckerImplTest {

    private lateinit var context: Context
    private lateinit var packageChecker: PackageChecker

    @Before
    fun setUp() {
        context = getInstrumentation().targetContext
        packageChecker = PackageCheckerImpl()
    }

    @Test
    fun isPackageInstalled_PackageExists_ReturnsTrue() {
        val result = packageChecker.isPackageInstalled(context, context.packageName)
        assertTrue(result)
    }

    @Test
    fun isPackageInstalled_PackageDoesNotExist_ReturnsFalse() {
        val result = packageChecker.isPackageInstalled(context, "non.existent.package")
        assertFalse(result)
    }
}
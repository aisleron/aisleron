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
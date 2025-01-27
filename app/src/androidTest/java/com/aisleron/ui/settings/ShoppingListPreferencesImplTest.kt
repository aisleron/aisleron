package com.aisleron.ui.settings

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.SharedPreferencesInitializer
import org.junit.Test
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
}
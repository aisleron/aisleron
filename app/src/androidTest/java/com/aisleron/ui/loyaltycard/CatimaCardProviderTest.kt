package com.aisleron.ui.loyaltycard

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.aisleron.R
import com.aisleron.domain.loyaltycard.LoyaltyCard
import com.aisleron.domain.loyaltycard.LoyaltyCardProviderType
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CatimaCardProviderTest {
    private lateinit var context: Context
    private lateinit var provider: CatimaCardProvider
    private val lookupActivityClassName = "protect.card_locker.CardShortcutConfigure"

    class TestFragment(private val loyaltyCardProvider: LoyaltyCardProvider) : Fragment() {
        private var _loyaltyCard: LoyaltyCard? = null
        val loyaltyCard: LoyaltyCard? get() = _loyaltyCard

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            loyaltyCardProvider.registerLauncher(this) {
                _loyaltyCard = it
            }
        }
    }

    @Before
    fun setUp() {
        context = getInstrumentation().targetContext
        provider = CatimaCardProvider(PackageCheckerTestImpl())
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun packageName_IsCorrect() {
        assertEquals("me.hackerchick.catima", provider.packageName)
    }

    @Test
    fun providerType_IsCatima() {
        assertEquals(LoyaltyCardProviderType.CATIMA, provider.providerType)
    }

    @Test(expected = com.aisleron.domain.base.AisleronException.LoyaltyCardProviderException::class)
    fun lookupLoyaltyCardShortcut_AppNotInstalled_ThrowsException() {
        (provider.packageChecker as PackageCheckerTestImpl).isPackageInstalledResult = false
        provider.lookupLoyaltyCardShortcut(context)
    }

    private fun getFragmentScenario(loyaltyCardProvider: LoyaltyCardProvider): FragmentScenario<TestFragment> {
        val scenario = launchFragmentInContainer<TestFragment>(
            themeResId = R.style.Theme_Aisleron,
            instantiate = {
                TestFragment(loyaltyCardProvider)
            }
        )

        return scenario
    }

    @Test
    fun registerLauncher_ResultOK_ReturnsLoyaltyCard() {
        (provider.packageChecker as PackageCheckerTestImpl).isPackageInstalledResult = true

        val shortcut = ShortcutInfoCompat.Builder(context, 1.toString())
            .setShortLabel("TestCard")
            .setLongLabel("TestCard")
            .setIntent(Intent(Intent.ACTION_VIEW, "Dummy Intent".toUri()))
            .build()

        val resultIntent = ShortcutManagerCompat.createShortcutResultIntent(context, shortcut)

        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent)
        val scenario = getFragmentScenario(provider)
        intending(hasComponent(lookupActivityClassName)).respondWith(result)
        provider.lookupLoyaltyCardShortcut(context)

        scenario.onFragment { fragment ->
            assertNotNull(fragment.loyaltyCard)
            assertEquals("TestCard", fragment.loyaltyCard?.name)
            assertEquals(LoyaltyCardProviderType.CATIMA, fragment.loyaltyCard?.provider)
        }
    }

    @Test
    fun registerLauncher_ResultCanceled_ReturnsNull() {
        (provider.packageChecker as PackageCheckerTestImpl).isPackageInstalledResult = true
        val result = Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)

        val scenario = getFragmentScenario(provider)
        intending(hasComponent(lookupActivityClassName)).respondWith(result)
        provider.lookupLoyaltyCardShortcut(context)

        scenario.onFragment { fragment ->
            assertNull(fragment.loyaltyCard)
        }
    }

    @Test(expected = com.aisleron.domain.base.AisleronException.LoyaltyCardProviderException::class)
    fun displayLoyaltyCard_ProviderNoyInstalled_ThrowLoyaltyCardProviderException() {
        (provider.packageChecker as PackageCheckerTestImpl).isPackageInstalledResult = false
        val loyaltyCard = LoyaltyCard(
            id = 1,
            name = "Test Card",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = "Dummy Intent"
        )

        provider.displayLoyaltyCard(context, loyaltyCard)
    }

    @Test
    fun displayLoyaltyCard_ProviderInstalled_RunIntent() {
        (provider.packageChecker as PackageCheckerTestImpl).isPackageInstalledResult = true
        val intent = Intent(Intent.ACTION_VIEW, provider.providerWebsite.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val loyaltyCard = LoyaltyCard(
            id = 1,
            name = "Test Card",
            provider = LoyaltyCardProviderType.CATIMA,
            intent = intent.toUri(Intent.URI_INTENT_SCHEME)
        )

        intending(
            allOf(hasAction(Intent.ACTION_VIEW), hasData(provider.providerWebsite.toUri()))
        ).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )

        provider.displayLoyaltyCard(context, loyaltyCard)

        intended(
            allOf(hasAction(Intent.ACTION_VIEW), hasData(provider.providerWebsite.toUri()))
        )
    }

    @Test
    fun getNotInstalledDialog_Called_DialogReturned() {
        getFragmentScenario(provider).onFragment { fragment ->
            val alertDialog = provider.getNotInstalledDialog(fragment.requireContext())
            alertDialog.show()
        }

        onView(withText(R.string.loyalty_card_provider_missing_title))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
    }

    @Test
    fun getNotInstalledDialog_OnCancelClicked_NoActionTaken() {
        var alertDialog: AlertDialog? = null

        getFragmentScenario(provider).onFragment { fragment ->
            alertDialog = provider.getNotInstalledDialog(fragment.requireContext())
            alertDialog?.show()
        }

        onView(withText(android.R.string.cancel))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        intended(hasAction(Intent.ACTION_VIEW), times(0))
        assertEquals(false, alertDialog?.isShowing)
    }

    @Test
    fun getNotInstalledDialog_OnOkClicked_ShowWebsite() {
        var alertDialog: AlertDialog? = null

        intending(
            allOf(hasAction(Intent.ACTION_VIEW), hasData(provider.providerWebsite.toUri()))
        ).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )

        getFragmentScenario(provider).onFragment { fragment ->
            alertDialog = provider.getNotInstalledDialog(fragment.requireContext())
            alertDialog?.show()
        }

        onView(withText(android.R.string.ok))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData(provider.providerWebsite.toUri())))
        assertEquals(false, alertDialog?.isShowing)
    }


    /**
     * To Test:
     * - getNotInstalledDialog
     *      - Dialog returned
     *      - Cancel does nothing
     *      - Ok launches intent
     */
}

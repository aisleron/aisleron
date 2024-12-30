package com.aisleron.ui.about

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.BuildConfig
import com.aisleron.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutFragment : PreferenceFragmentCompat() {

    private val aboutViewModel: AboutViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)

        findPreference<Preference>("version")?.let {
            it.summary = BuildConfig.VERSION_NAME
        }
    }

}
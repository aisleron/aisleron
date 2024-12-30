package com.aisleron.ui.about

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutFragment : PreferenceFragmentCompat() {

    private val aboutViewModel: AboutViewModel by viewModel()

    enum class AboutOption(val key: String) {
        VERSION("about_support_version"),
        REPORT_ISSUE("about_support_report_issue"),
        SOURCE_CODE("about_support_sourcecode"),
        LICENSE("about_legal_license"),
        PRIVACY("about_legal_privacy"),
        TERMS_AND_CONDITIONS("about_legal_tnc")
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)

        findPreference<Preference>(AboutOption.VERSION.key)?.let {
            it.summary = aboutViewModel.versionName
        }
    }
}
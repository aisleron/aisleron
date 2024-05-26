package com.aisleron.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}
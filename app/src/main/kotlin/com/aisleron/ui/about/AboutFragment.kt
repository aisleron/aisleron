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

package com.aisleron.ui.about

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aisleron.R
import com.aisleron.ui.AisleronFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutFragment : PreferenceFragmentCompat(), AisleronFragment {

    private val aboutViewModel: AboutViewModel by viewModel()

    enum class AboutOption(val key: String) {
        VERSION("about_support_version"),
//        REPORT_ISSUE("about_support_report_issue"),
//        SOURCE_CODE("about_support_sourcecode"),
//        LICENSE("about_legal_license"),
//        PRIVACY("about_legal_privacy"),
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.about, rootKey)

        findPreference<Preference>(AboutOption.VERSION.key)?.let {
            it.summary =
                getString(R.string.about_support_version_summary, aboutViewModel.versionName)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setWindowInsetListeners(this, view, false, null)
    }
}
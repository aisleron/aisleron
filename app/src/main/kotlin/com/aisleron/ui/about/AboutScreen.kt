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

package com.aisleron.ui.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.aisleron.BuildConfig
import com.aisleron.R
import com.aisleron.ui.component.AisleronScreen
import com.aisleron.ui.component.preference.PreferenceCategory
import com.aisleron.ui.component.preference.UrlPreference
import com.aisleron.ui.theme.AisleronTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AboutScreen(
    onBackPressed: () -> Unit,
    onUrlClick: (String) -> Unit,
    viewModel: AboutViewModel = koinViewModel()
) {
    AboutScreenContent(
        versionName = viewModel.versionName,
        onBackPressed = onBackPressed,
        onUrlClick = onUrlClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenContent(
    versionName: String,
    onBackPressed: () -> Unit,
    onUrlClick: (String) -> Unit
) {
    AisleronScreen(
        title = stringResource(R.string.title_activity_about),
        onBackPressed = onBackPressed
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            item {
                PreferenceCategory(title = stringResource(R.string.about_support_header)) {
                    // Version History
                    UrlPreference(
                        title = stringResource(R.string.about_support_version_title),
                        summary = stringResource(
                            R.string.about_support_version_summary, versionName
                        ),
                        urlResId = R.string.aisleron_version_history_url,
                        onUrlClick = onUrlClick
                    )

                    // Documentation
                    UrlPreference(
                        title = stringResource(R.string.about_support_documentation_title),
                        summary = stringResource(R.string.about_support_documentation_summary),
                        urlResId = R.string.aisleron_documentation_url,
                        onUrlClick = onUrlClick
                    )

                    // Report Issue
                    UrlPreference(
                        title = stringResource(R.string.about_support_report_issue_title),
                        summary = stringResource(R.string.about_support_report_issue_summary),
                        urlResId = R.string.aisleron_reporting_issues_url,
                        onUrlClick = onUrlClick
                    )

                    // Source Code
                    UrlPreference(
                        title = stringResource(R.string.about_support_sourcecode_title),
                        summary = stringResource(R.string.about_support_sourcecode_summary),
                        urlResId = R.string.aisleron_sourcecode_url,
                        onUrlClick = onUrlClick
                    )
                }
            }


            // Contribute Section
            item {
                PreferenceCategory(title = stringResource(R.string.about_contribute_header)) {
                    // Translations
                    UrlPreference(
                        title = stringResource(R.string.about_contribute_translate_title),
                        summary = stringResource(R.string.about_contribute_translate_summary),
                        urlResId = R.string.aisleron_translate_url,
                        onUrlClick = onUrlClick
                    )


                    // Financial Contribution
                    UrlPreference(
                        title = stringResource(R.string.about_contribute_financial_title),
                        summary = stringResource(R.string.about_contribute_financial_summary),
                        urlResId = R.string.aisleron_financial_contribution_url,
                        onUrlClick = onUrlClick
                    )
                }
            }

            // Legal Section
            item {
                PreferenceCategory(title = stringResource(R.string.about_legal_header)) {
                    // Aisleron License
                    UrlPreference(
                        title = stringResource(R.string.about_legal_license_title),
                        summary = stringResource(R.string.about_legal_license_summary),
                        urlResId = R.string.aisleron_license_url,
                        onUrlClick = onUrlClick
                    )

                    // Privacy Policy
                    UrlPreference(
                        title = stringResource(R.string.about_legal_privacy_title),
                        summary = stringResource(R.string.about_legal_privacy_summary),
                        urlResId = R.string.aisleron_privacy_policy_url,
                        onUrlClick = onUrlClick
                    )

                    // Third Party Licences
                    UrlPreference(
                        title = stringResource(R.string.about_legal_3rdparty_title),
                        summary = stringResource(R.string.about_legal_3rdparty_summary),
                        urlResId = R.string.aisleron_3rd_party_licenses_url,
                        onUrlClick = onUrlClick
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, name = "About Screen Light Mode")
@Preview(
    showSystemUi = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    name = "About Screen Dark Mode"
)
@Composable
fun AboutScreenContentPreview() {
    AisleronTheme {
        AboutScreenContent(
            versionName = BuildConfig.VERSION_NAME,
            onBackPressed = {},
            onUrlClick = {}
        )
    }
}


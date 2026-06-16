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

package com.aisleron.ui.component.preference

import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.aisleron.R

@Composable
fun UrlPreference(
    title: String,
    summary: String,
    @StringRes urlResId: Int,
    onUrlClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val url = stringResource(urlResId)

    Preference(
        title = title,
        summary = summary,
        onClick = { onUrlClick(url) },
        modifier = modifier,
        icon = icon,
        control = {
            Icon(
                painter = painterResource(R.drawable.baseline_launch_24),
                contentDescription = null
            )
        }
    )
}
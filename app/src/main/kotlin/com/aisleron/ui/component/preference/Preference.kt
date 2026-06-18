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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

private fun setColor(baseColor: Color, enabled: Boolean): Color =
    if (enabled) baseColor else baseColor.copy(alpha = 0.38f)

@Composable
fun Preference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    // Slot for an optional leading icon
    @DrawableRes iconResId: Int? = null,
    @StringRes iconContentDescriptionResId: Int? = null,
    // Slot for interactive widgets like Checkboxes, Switches, or Action Icons
    control: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .then(
                if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Side Slot: Optional Icon Bounding Box
        if (iconResId != null) {
            Icon(
                painter = painterResource(iconResId),
                contentDescription = iconContentDescriptionResId?.let { stringResource(it) },
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
                tint = setColor(MaterialTheme.colorScheme.onSurfaceVariant, enabled)
            )
        }

        // Centre Column: Text Information
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = setColor(MaterialTheme.colorScheme.onSurfaceVariant, enabled)
            )
            if (summary != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = setColor(MaterialTheme.colorScheme.onSurfaceVariant, enabled)
                )
            }
        }

        // Right Side Slot: Control Component
        if (control != null) {
            Box(modifier = Modifier.padding(start = 16.dp)) {
                control()
            }
        }
    }
}
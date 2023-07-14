package com.illusionware.npsbrowser.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NPSFloatingActionButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.5f)
            .then(if (enabled) Modifier.shadow(6.dp, shape) else Modifier.clip(shape))
            .then(if (enabled) Modifier.combinedClickable(
                onClick = { onClick() },
                onLongClick = {
                    if (onLongClick != null) {
                        onLongClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            ) else Modifier)
            .semantics(
                mergeDescendants = true,
                properties = {
                    if (!enabled) disabled()
                    onClick { onClick(); true }
                    if (onLongClick != null) {
                        onLongClick { onLongClick(); true }
                    }
                    role = Role.Button
                },
            ),
        color = containerColor,
        tonalElevation = 3.dp,
        shadowElevation = elevation,
        contentColor = contentColor,
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                content()
            }
        }
    }
}

@Composable
@Preview
fun NPSFABPreview() {
    NPSFloatingActionButton(
        onClick = {},
    ) {
        Icon(imageVector = Icons.Outlined.Eco, contentDescription = "Eco")
    }
}
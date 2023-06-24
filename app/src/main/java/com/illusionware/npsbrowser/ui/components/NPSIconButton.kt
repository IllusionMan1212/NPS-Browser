package com.illusionware.npsbrowser.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NPSIconButton(tooltip: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    PlainTooltipBox(tooltip = {
        Text(text = tooltip)
    }) {
        IconButton(onClick = onClick, Modifier.tooltipAnchor()) {
            content()
        }
    }
}
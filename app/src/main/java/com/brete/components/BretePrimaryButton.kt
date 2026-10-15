package com.brete.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BretePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true // <-- Añade esta línea
) {
    Button(
        onClick = onClick,
        enabled = enabled, // <-- Pásale el valor al Button base
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Text(text = text)
    }
}
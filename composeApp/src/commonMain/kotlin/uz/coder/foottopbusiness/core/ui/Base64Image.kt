package uz.coder.foottopbusiness.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun Base64Image(
    base64: String,
    modifier: Modifier = Modifier
)
package uz.coder.foottopbusiness.core

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, content: () -> Unit) {
    BackHandler(enabled, onBack = content)
}
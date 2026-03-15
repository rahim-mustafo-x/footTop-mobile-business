package uz.coder.foottopbusiness.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val content: @Composable () -> Unit
)

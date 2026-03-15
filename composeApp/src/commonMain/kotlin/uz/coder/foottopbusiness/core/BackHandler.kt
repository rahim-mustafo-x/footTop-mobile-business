package uz.coder.foottopbusiness.core

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(enabled: Boolean = true, content:()-> Unit)
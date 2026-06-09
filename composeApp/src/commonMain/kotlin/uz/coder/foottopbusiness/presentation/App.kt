package uz.coder.foottopbusiness.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import uz.coder.foottopbusiness.core.localization.ProvideLocalization
import uz.coder.foottopbusiness.core.ui.AppTheme
import uz.coder.foottopbusiness.core.ui.DebugFloatingButton
import uz.coder.foottopbusiness.presentation.navigation.AppNavigation

@Composable
@Preview(showSystemUi = true)
fun App() {
    AppTheme {
        ProvideLocalization {
            AppNavigation()
            DebugFloatingButton()
        }
    }
}

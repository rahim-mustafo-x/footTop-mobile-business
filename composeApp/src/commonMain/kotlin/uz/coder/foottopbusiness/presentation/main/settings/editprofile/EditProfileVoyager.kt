package uz.coder.foottopbusiness.presentation.main.settings.editprofile

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import org.koin.compose.koinInject

object EditProfileVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel: EditProfileViewModel = koinInject()
        EditProfileScreen(viewModel)
    }
}
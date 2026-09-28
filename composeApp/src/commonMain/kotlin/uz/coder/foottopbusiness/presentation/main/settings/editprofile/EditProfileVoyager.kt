package uz.coder.foottopbusiness.presentation.main.settings.editprofile

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

object EditProfileVoyager : Screen {
    @Composable
    override fun Content() {
        val viewModel= getScreenModel<EditProfileViewModel>()
        EditProfileScreen(viewModel)
    }
}
package uz.coder.foottopbusiness

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import uz.coder.foottopbusiness.di.appModule
import uz.coder.foottopbusiness.di.platformModule
import uz.coder.foottopbusiness.presentation.App

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}

fun initKoin() {
    startKoin {
        modules(appModule, platformModule())
    }
}

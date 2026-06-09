package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.local.PreferencesManager
import io.ktor.client.engine.darwin.Darwin

import uz.coder.foottopbusiness.core.notification.IosPushTokenProvider
import uz.coder.foottopbusiness.core.notification.PushTokenProvider

actual fun platformModule(): Module {
    return module {
        single { PreferencesManager() }
        single<PushTokenProvider> { IosPushTokenProvider() }
        single { Darwin.create() }
    }
}

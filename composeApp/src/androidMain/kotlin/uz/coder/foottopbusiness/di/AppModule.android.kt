package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.local.PreferencesManager
import org.koin.android.ext.koin.androidContext

import uz.coder.foottopbusiness.core.notification.AndroidPushTokenProvider
import uz.coder.foottopbusiness.core.notification.PushTokenProvider

actual fun platformModule(): Module {
    return module {
        single { PreferencesManager(androidContext()) }
        single<PushTokenProvider> { AndroidPushTokenProvider() }
    }
}
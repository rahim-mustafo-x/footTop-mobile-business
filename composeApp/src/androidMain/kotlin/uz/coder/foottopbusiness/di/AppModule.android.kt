package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.local.PreferencesManager
import org.koin.android.ext.koin.androidContext
import io.ktor.client.engine.okhttp.OkHttp
import com.chuckerteam.chucker.api.ChuckerInterceptor
import android.content.pm.ApplicationInfo

import uz.coder.foottopbusiness.core.notification.AndroidPushTokenProvider
import uz.coder.foottopbusiness.core.notification.PushTokenProvider

actual fun platformModule(): Module {
    return module {
        single { PreferencesManager(androidContext()) }
        single<PushTokenProvider> { AndroidPushTokenProvider() }
        single {
            OkHttp.create {
                val isDebug = (androidContext().applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
                if (isDebug) {
                    addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
                }
            }
        }
    }
}

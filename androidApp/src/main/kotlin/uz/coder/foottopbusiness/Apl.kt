package uz.coder.foottopbusiness

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import uz.coder.foottopbusiness.core.context.ContextProvider
import uz.coder.foottopbusiness.di.appModule
import uz.coder.foottopbusiness.di.platformModule

class Apl: Application() {
    override fun onCreate() {
        super.onCreate()
        ContextProvider.init(this)
        initKoin()
    }
    private fun initKoin() {
        startKoin {
            androidContext(this@Apl)
            modules(appModule, platformModule())
        }
    }
}
package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.HttpClientFactory
import uz.coder.foottopbusiness.presentation.auth.login.LoginViewModel
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpViewModel

val appModule = module {
    //api service
    single { HttpClientFactory.create(get()) }
    single { AuthApiService(get()) }

    //viewModel
    factory { SendOtpViewModel() }
    factory { LoginViewModel() }
}

expect fun platformModule(): Module
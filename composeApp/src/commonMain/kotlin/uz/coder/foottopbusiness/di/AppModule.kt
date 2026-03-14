package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.HttpClientFactory
import uz.coder.foottopbusiness.data.repository.AuthRepositoryImpl
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase
import uz.coder.foottopbusiness.presentation.auth.login.LoginViewModel
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpViewModel

val appModule = module {
    //api service
    single { HttpClientFactory.create(get()) }
    single { AuthApiService(get()) }

    //viewModel
    factory { SendOtpViewModel(get()) }
    factory { LoginViewModel() }

    //repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    //use-case
    factory { SendOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
}

expect fun platformModule(): Module
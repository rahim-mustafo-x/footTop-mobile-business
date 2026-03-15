package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.HttpClientFactory
import uz.coder.foottopbusiness.data.repository.AuthRepositoryImpl
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase
import uz.coder.foottopbusiness.presentation.auth.login.LoginViewModel
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpViewModel
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesViewModel
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel
import uz.coder.foottopbusiness.presentation.main.settings.SettingsViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel

val appModule = module {
    //api service
    single { HttpClientFactory.create(get()) }
    single { AuthApiService(get()) }

    //viewModel
    factory { SendOtpViewModel(get(), get()) }
    factory { LoginViewModel(get(), get()) }
    factory { HomeViewModel() }
    factory { StadiumViewModel() }
    factory { AddPitchViewModel() }
    factory { CoachesViewModel() }
    factory { TournamentsViewModel() }
    factory { SettingsViewModel(get()) }

    //repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }

    //use-case
    factory { SendOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { IsLoginInUseCase(get()) }
}

expect fun platformModule(): Module
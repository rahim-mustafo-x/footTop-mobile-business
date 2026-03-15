package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.StadiumApiService
import uz.coder.foottopbusiness.data.network.HttpClientFactory
import uz.coder.foottopbusiness.data.repository.AuthRepositoryImpl
import uz.coder.foottopbusiness.data.repository.StadiumRepositoryImpl
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.CreateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedDistrictIdUseCase
import uz.coder.foottopbusiness.presentation.auth.login.LoginViewModel
import uz.coder.foottopbusiness.presentation.auth.otp.SendOtpViewModel
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesViewModel
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel
import uz.coder.foottopbusiness.presentation.main.settings.SettingsViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.addpitch.AddPitchViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel
import uz.coder.foottopbusiness.presentation.splash.SplashViewModel

val appModule = module {
    //api service
    single { HttpClientFactory.create(get()) }
    single { AuthApiService(get()) }
    single { StadiumApiService(get()) }

    //repository
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<StadiumRepository> { StadiumRepositoryImpl(get(), get()) }

    //use-case
    factory { SendOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { IsLoginInUseCase(get()) }
    factory { CreateStadiumUseCase(get()) }
    factory { GetRegionsUseCase(get()) }
    factory { GetDistrictsUseCase(get()) }
    factory { SaveRegionIdUseCase(get()) }
    factory { SaveDistrictIdUseCase(get()) }
    factory { GetSavedRegionIdUseCase(get()) }
    factory { GetSavedDistrictIdUseCase(get()) }

    //viewModel
    factory { SplashViewModel(get()) }
    factory { SendOtpViewModel(get(), get()) }
    factory { LoginViewModel(get(), get()) }
    factory { HomeViewModel() }
    factory { StadiumViewModel(get(), get(), get()) }
    factory { AddPitchViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { CoachesViewModel() }
    factory { TournamentsViewModel() }
    factory { SettingsViewModel(get()) }
}

expect fun platformModule(): Module

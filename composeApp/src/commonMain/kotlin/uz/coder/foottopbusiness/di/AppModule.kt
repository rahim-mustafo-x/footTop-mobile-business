package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.data.network.AuthApiService
import uz.coder.foottopbusiness.data.network.CoachApiService
import uz.coder.foottopbusiness.data.network.HttpClientFactory
import uz.coder.foottopbusiness.data.network.MatchApiService
import uz.coder.foottopbusiness.data.network.StadiumApiService
import uz.coder.foottopbusiness.data.network.TournamentApiService
import uz.coder.foottopbusiness.data.network.UserApiService
import uz.coder.foottopbusiness.data.repository.AuthRepositoryImpl
import uz.coder.foottopbusiness.data.repository.CoachRepositoryImpl
import uz.coder.foottopbusiness.data.repository.MatchRepositoryImpl
import uz.coder.foottopbusiness.data.repository.StadiumRepositoryImpl
import uz.coder.foottopbusiness.data.repository.TournamentRepositoryImpl
import uz.coder.foottopbusiness.data.repository.UserRepositoryImpl
import uz.coder.foottopbusiness.domain.repository.AuthRepository
import uz.coder.foottopbusiness.domain.repository.CoachRepository
import uz.coder.foottopbusiness.domain.repository.MatchRepository
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import uz.coder.foottopbusiness.domain.repository.TournamentRepository
import uz.coder.foottopbusiness.domain.repository.UserRepository
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.SendOtpUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.CreateCoachUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.CreateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.CreateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase
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
    // core
    single { SessionManager() }
    // PreferencesManager + SessionManager — token mismatch fix uchun
    single { HttpClientFactory.create(get(), get()) }

    // api services
    single { AuthApiService(get()) }
    single { StadiumApiService(get()) }
    single { CoachApiService(get()) }
    single { TournamentApiService(get()) }
    single { MatchApiService(get()) }
    single { UserApiService(get()) }

    // repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<StadiumRepository> { StadiumRepositoryImpl(get(), get()) }
    single<CoachRepository> { CoachRepositoryImpl(get(), get()) }
    single<TournamentRepository> { TournamentRepositoryImpl(get(), get()) }
    single<MatchRepository> { MatchRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }

    // use-cases
    factory { SendOtpUseCase(get()) }
    factory { LoginUseCase(get()) }
    factory { IsLoginInUseCase(get()) }
    factory { CreateStadiumUseCase(get()) }
    factory { GetStadiumsUseCase(get()) }
    factory { DeleteStadiumUseCase(get()) }
    factory { GetRegionsUseCase(get()) }
    factory { GetDistrictsUseCase(get()) }
    factory { SaveRegionIdUseCase(get()) }
    factory { SaveDistrictIdUseCase(get()) }
    factory { GetSavedRegionIdUseCase(get()) }
    factory { GetSavedDistrictIdUseCase(get()) }
    factory { GetCoachesUseCase(get()) }
    factory { CreateCoachUseCase(get()) }
    factory { GetTournamentsUseCase(get()) }
    factory { CreateTournamentUseCase(get()) }
    factory { GetMatchesUseCase(get()) }
    factory { GetUserUseCase(get()) }

    // viewModels
    factory { SplashViewModel(get()) }
    factory { SendOtpViewModel(get(), get()) }
    factory { LoginViewModel(get(), get()) }
    factory { HomeViewModel(get(), get(), get(), get()) }
    factory { StadiumViewModel(get(), get(), get()) }
    factory { AddPitchViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { CoachesViewModel(get(), get()) }
    factory { TournamentsViewModel(get(), get(), get()) }
    factory { SettingsViewModel(get(), get()) }
}

expect fun platformModule(): Module

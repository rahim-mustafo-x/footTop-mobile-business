package uz.coder.foottopbusiness.di

import org.koin.core.module.Module
import org.koin.dsl.module
import uz.coder.foottopbusiness.core.SessionManager
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.data.network.*
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.data.repository.*
import uz.coder.foottopbusiness.domain.repository.*
import uz.coder.foottopbusiness.domain.usecase.admin.CreateStaffUseCase
import uz.coder.foottopbusiness.domain.usecase.admin.DashboardUseCase
import uz.coder.foottopbusiness.domain.usecase.admin.WeeklyReportUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.CancelBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsByStadiumIdUseCase
import uz.coder.foottopbusiness.domain.usecase.booking.GetBookingsUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.ChangePasswordUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.IsLoginInUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LoginUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.CreateCoachUseCase
import uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.notification.RegisterDeviceTokenUseCase
import uz.coder.foottopbusiness.domain.usecase.notification.SendNotificationUseCase
import uz.coder.foottopbusiness.domain.usecase.notification.SendToAllUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.CreateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetDistrictsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetRegionsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumByIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.SaveRegionIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateOpenCloseTimeUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.CreateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.UpdateTournamentUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetAllUsersUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GeneratePasswordUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase
import uz.coder.foottopbusiness.domain.usecase.user.UserIdUseCase
import uz.coder.foottopbusiness.presentation.auth.login.LoginViewModel
import uz.coder.foottopbusiness.presentation.main.booking.list.BookingListViewModel
import uz.coder.foottopbusiness.presentation.main.coaches.CoachesViewModel
import uz.coder.foottopbusiness.presentation.main.home.HomeViewModel
import uz.coder.foottopbusiness.presentation.main.home.user.UserCreateViewModel
import uz.coder.foottopbusiness.presentation.main.settings.SettingsViewModel
import uz.coder.foottopbusiness.presentation.main.settings.editprofile.EditProfileViewModel
import uz.coder.foottopbusiness.presentation.main.settings.notification.SendNotificationViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.StadiumViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.addstadium.AddStadiumViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.details.StadiumDetailsViewModel
import uz.coder.foottopbusiness.presentation.main.stadium.edit.EditStadiumViewModel
import uz.coder.foottopbusiness.presentation.main.tournaments.TournamentsViewModel
import uz.coder.foottopbusiness.presentation.splash.SplashViewModel

val appModule = module {
    // core
    single { UserSession() }
    single { SessionManager(get()) }
    single { HttpClientFactory(get(), get()) }
    single { get<HttpClientFactory>().create() }

    // api services
    single { AuthApiService(get()) }
    single { StadiumApiService(get()) }
    single { CoachApiService(get()) }
    single { TournamentApiService(get()) }
    single { MatchApiService(get()) }
    single { UserApiService(get()) }
    single { NotificationApiService(get()) }
    single { AdminApiService(get()) }
    single { BookingApiService(get()) }

    // repositories
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }
    single<StadiumRepository> { StadiumRepositoryImpl(get(), get()) }
    single<CoachRepository> { CoachRepositoryImpl(get()) }
    single<TournamentRepository> { TournamentRepositoryImpl(get()) }
    single<MatchRepository> { MatchRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<AdminRepository> { AdminRepositoryImpl(get()) }
    single<BookingRepository> { BookingRepositoryImpl(get()) }

    // use-cases
    factory { LoginUseCase(get()) }
    factory { IsLoginInUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ChangePasswordUseCase(get()) }
    factory { CreateStadiumUseCase(get()) }
    factory { UpdateStadiumUseCase(get()) }
    factory { GetStadiumsUseCase(get()) }
    factory { GetStadiumByIdUseCase(get()) }
    factory { UpdateOpenCloseTimeUseCase(get()) }
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
    factory { UpdateTournamentUseCase(get()) }
    factory { GetMatchesUseCase(get()) }
    factory { GetUserUseCase(get()) }
    factory { GetAllUsersUseCase(get()) }
    factory { GeneratePasswordUseCase(get()) }
    factory { UserIdUseCase(get()) }
    factory { SendNotificationUseCase(get()) }
    factory { SendToAllUseCase(get()) }
    factory { RegisterDeviceTokenUseCase(get()) }
    factory { DashboardUseCase(get()) }
    factory { WeeklyReportUseCase(get()) }
    factory { CreateStaffUseCase(get()) }
    factory { CreateBookingUseCase(get()) }
    factory { CancelBookingUseCase(get()) }
    factory { GetBookingsByStadiumIdUseCase(get()) }
    factory { GetBookingsUseCase(get()) }


    // viewModels
    factory { SplashViewModel(get(), get(), get(), get()) }
    factory { LoginViewModel(get(), get(), get(), get()) }
    factory { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { StadiumViewModel(get(), get(), get(), get()) }
    factory { AddStadiumViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { (stadium: StadiumResponse) -> EditStadiumViewModel(stadium, get(), get(), get(), get(), get()) }
    factory { (stadium: StadiumResponse) -> StadiumDetailsViewModel(stadium, get(), get(), get(), get(), get()) }
    factory { BookingListViewModel(get(), get()) }
    factory { CoachesViewModel(get(), get(), get()) }
    factory { TournamentsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    factory { SettingsViewModel(get(), get(), get()) }
    factory { EditProfileViewModel(get(), get()) }
    factory { SendNotificationViewModel(get()) }
    factory { UserCreateViewModel(get(), get(), get(), get(), get(), get()) }
}

expect fun platformModule(): Module

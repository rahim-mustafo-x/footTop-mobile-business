package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.lifecycle.viewModelScope
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.admin.DashboardUseCase
import uz.coder.foottopbusiness.domain.usecase.admin.WeeklyReportUseCase
import uz.coder.foottopbusiness.domain.usecase.auth.LogoutUseCase
import uz.coder.foottopbusiness.domain.usecase.match.GetMatchesUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.DeleteStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumByIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetStadiumsUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.UpdateOpenCloseTimeUseCase
import uz.coder.foottopbusiness.domain.usecase.tournament.GetTournamentsUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetAllUsersUseCase
import uz.coder.foottopbusiness.domain.usecase.user.GetUserUseCase
import uz.coder.foottopbusiness.data.local.PreferencesManager
import uz.coder.foottopbusiness.domain.usecase.booking.CreateBookingUseCase
import uz.coder.foottopbusiness.data.network.dto.booking.BookingRequestDto
import uz.coder.foottopbusiness.core.platform.checkNotificationPermissionStatus
import uz.coder.foottopbusiness.core.platform.requestNotificationPermission
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.core.platform.openAppSettings
import uz.coder.foottopbusiness.core.UserSession
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.home.HomeContract.Effect.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private fun durationMinutesKey(key: String): Int = when(key) {
    "SIXTY" -> 60
    "NINETY" -> 90
    "ONE_HUNDRED_TWENTY" -> 120
    else -> 60
}

class HomeViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val deleteStadiumUseCase: DeleteStadiumUseCase,
    private val updateOpenCloseTimeUseCase: UpdateOpenCloseTimeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getStadiumByIdUseCase: GetStadiumByIdUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val preferencesManager: PreferencesManager,
    private val getCoachesUseCase: uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase,
    private val dashboardUseCase: DashboardUseCase,
    private val weeklyReportUseCase: WeeklyReportUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val userSession: UserSession
) : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    initialState = HomeContract.State(
        selectedDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    )
) {
    init {
        val currentUser = userSession.user.value
        if (currentUser != null) {
            val role = userSession.role.value
            updateState { 
                copy(
                    user = currentUser,
                    isAdmin = role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN,
                    isOwner = role == UserRole.OWNER,
                    userRole = role,
                    isLoadingUser = false
                ) 
            }
        }
        handleEvent(HomeContract.Event.Load)
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.Load, HomeContract.Event.Refresh -> {
                updateState { copy(currentPage = 0, stadiums = emptyList(), isLastPage = false) }
                loadUser()
                loadStadiums(0)
                loadCoaches()
                loadMatches()
                loadTournaments()
            }
            
            is HomeContract.Event.SetShowStadiumTable -> updateState { copy(showStadiumTable = event.show) }

            is HomeContract.Event.Search -> {
                updateState { copy(searchQuery = event.query, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }

            is HomeContract.Event.FilterActive -> {
                updateState { copy(filterActive = event.isActive, currentPage = 0, stadiums = emptyList()) }
                loadStadiums(0)
            }

            HomeContract.Event.LoadNextPage -> {
                val s = state.value
                if (!s.isLastPage && !s.isLoadingStadiums) loadStadiums(s.currentPage + 1)
            }

            HomeContract.Event.LoadPreviousPage -> {
                val s = state.value
                if (s.currentPage > 0 && !s.isLoadingStadiums) loadStadiums(s.currentPage - 1)
            }

            is HomeContract.Event.LoadPage -> {
                if (!state.value.isLoadingStadiums) loadStadiums(event.page)
            }

            is HomeContract.Event.DeleteRequest -> updateState { copy(deletingId = event.id) }
            HomeContract.Event.DeleteCancel -> updateState { copy(deletingId = null) }
            HomeContract.Event.DeleteConfirm -> {
                val id = state.value.deletingId ?: return
                updateState { copy(deletingId = null) }
                executeAsync {
                    deleteStadiumUseCase(id).collect {
                        updateState { copy(stadiums = stadiums.filter { it.id != id }) }
                        sendEffect(ShowToast("Stadion o'chirildi"))
                        loadDashboardStats()
                    }
                }
            }

            is HomeContract.Event.SelectTournament -> updateState { copy(selectedTournament = event.t) }
            HomeContract.Event.ClearTournament -> updateState { copy(selectedTournament = null) }
            is HomeContract.Event.SelectMatch -> updateState { copy(selectedMatch = event.m) }
            HomeContract.Event.ClearMatch -> updateState { copy(selectedMatch = null) }

            is HomeContract.Event.SelectStadiumForSlots -> {
                updateState { 
                    copy(
                        selectedStadiumForTime = event.stadium,
                        newOpenTime = event.stadium.openTime ?: "",
                        newCloseTime = event.stadium.closeTime ?: ""
                    ) 
                }
                loadSlots(event.stadium.id ?: return, state.value.selectedDate, state.value.selectedDuration)
            }

            is HomeContract.Event.ChangeDate -> {
                updateState { copy(selectedDate = event.date) }
                state.value.selectedStadiumForTime?.id?.let { loadSlots(it, event.date, state.value.selectedDuration) }
            }

            is HomeContract.Event.ChangeDuration -> {
                updateState { copy(selectedDuration = event.duration) }
                state.value.selectedStadiumForTime?.id?.let { loadSlots(it, state.value.selectedDate, event.duration) }
            }

            is HomeContract.Event.SelectSlot -> {
                val availableSlots = state.value.stadiumSlots
                val slotIndex = availableSlots.indexOf(event.slot)
                if (slotIndex == -1) return

                val duration = state.value.selectedDuration
                val slotsToSelect = when (duration) {
                    "SIXTY" -> 3
                    "NINETY" -> 4
                    "ONE_HUNDRED_TWENTY" -> 5
                    else -> 1
                }

                // Check only slots within the range, excluding the boundary end point
                val canBook = if (slotIndex + slotsToSelect <= availableSlots.size) {
                    (slotIndex until (slotIndex + slotsToSelect - 1)).all { availableSlots[it].third }
                } else {
                    false
                }

                if (canBook) {
                    updateState { copy(selectedSlot = event.slot, isBookingSlot = true) }
                } else {
                    sendEffect(ShowToast("Tanlangan vaqt oralig'ida bo'sh joy yetarli emas"))
                }
            }

            is HomeContract.Event.CreateBooking -> {
                val s = state.value
                val slot = s.selectedSlot ?: return
                val stadium = s.selectedStadiumForTime ?: return
                
                updateState { copy(isBookingSlot = false, selectedSlot = null) }
                
                executeAsync(
                    block = {
                        val userId = preferencesManager.userId.first().toLong()
                        val duration = s.selectedDuration
                        val slotsToSelect = when (duration) {
                            "SIXTY" -> 3
                            "NINETY" -> 4
                            "ONE_HUNDRED_TWENTY" -> 5
                            else -> 1
                        }
                        
                        val availableSlots = s.stadiumSlots
                        val slotIndex = availableSlots.indexOf(slot)
                        val endSlot = availableSlots.getOrNull(slotIndex + slotsToSelect - 1)
                        
                        val totalPrice = (stadium.pricePerHour ?: 0.0) * (durationMinutesKey(duration) / 60.0)
                        
                        val request = BookingRequestDto(
                            userId = userId,
                            stadiumId = stadium.id?.toLong(),
                            startTime = slot.first.toString(),
                            endTime = endSlot?.first?.toString() ?: slot.second.toString(),
                            totalPrice = totalPrice,
                            status = "PENDING",
                            paymentMethod = "CASH"
                        )
                        createBookingUseCase(request).first()
                    },
                    onSuccess = {
                        sendEffect(ShowToast("Muvaffaqiyatli band qilindi: ${event.name}"))
                        loadSlots(stadium.id ?: return@executeAsync, s.selectedDate, s.selectedDuration)
                        // Check for notification permission after successful booking
                        handleEvent(HomeContract.Event.CheckNotificationPermission)
                    },
                    onError = {
                        sendEffect(ShowToast("Xatolik: ${it.message}"))
                    }
                )
            }

            HomeContract.Event.DismissBookingDialog -> updateState { copy(isBookingSlot = false) }

            HomeContract.Event.ClearStadiumForSlots -> updateState { copy(selectedStadiumForTime = null, stadiumSlots = emptyList(), selectedSlot = null, isBookingSlot = false) }

            is HomeContract.Event.UpdateTime -> {
                val stadiumId = state.value.selectedStadiumForTime?.id ?: return
                updateState { copy(isUpdatingTime = true) }
                executeAsync {
                    updateOpenCloseTimeUseCase(stadiumId, event.open, event.close).collect {
                        updateState {
                            copy(
                                isUpdatingTime = false,
                                selectedStadiumForTime = null,
                                stadiums = stadiums.map {
                                    if (it.id == stadiumId) it.copy(
                                        openTime = event.open,
                                        closeTime = event.close
                                    ) else it
                                }
                            )
                        }
                        sendEffect(ShowToast("Vaqt yangilandi"))
                    }
                }
            }

            HomeContract.Event.DownloadReport -> {
                executeAsync {
                    val matches = state.value.matches
                    val csv = StringBuilder()
                    csv.append("ID,Sana,Nomi,Stadion ID,O'yinchilar,Narx,Jami\n")
                    matches.forEach { match ->
                        val total = (match.currentPlayers ?: 0) * (match.pricePerPlayer ?: 0.0)
                        csv.append("${match.id},${match.dateTime},${match.title},${match.stadiumId},${match.currentPlayers},${match.pricePerPlayer},$total\n")
                    }
                    sendEffect(DownloadFile("hisobot_${state.value.selectedDate}.csv", csv.toString()))
                }
            }

            HomeContract.Event.Logout -> {
                executeAsync {
                    logoutUseCase()
                }
            }

            HomeContract.Event.ShowExitToast -> sendEffect(ShowToast("Chiqish uchun yana bir marta bosing"))

            HomeContract.Event.Match -> sendEffect(Match)
            HomeContract.Event.Stadium -> sendEffect(Stadium)
            HomeContract.Event.Tournament -> sendEffect(Tournament)

            is HomeContract.Event.SetShowNotificationPermissionDialog -> updateState { copy(showNotificationPermissionDialog = event.show) }
            HomeContract.Event.RequestNotificationPermission -> {
                updateState { copy(showNotificationPermissionDialog = false, triggerNotificationRequest = true) }
            }
            is HomeContract.Event.OnNotificationPermissionResult -> {
                updateState { copy(triggerNotificationRequest = false) }
                handlePermissionResult(event.status)
            }
            HomeContract.Event.CheckNotificationPermission -> {
                checkPermission()
            }
            HomeContract.Event.DismissPermanentlyDeniedDialog -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
            }
            HomeContract.Event.OpenSettings -> {
                updateState { copy(showPermanentlyDeniedDialog = false) }
                openAppSettings()
            }
        }
    }

    private fun handlePermissionResult(status: PermissionStatus) {
        viewModelScope.launch {
            val currentCount = preferencesManager.notificationRequestCount.first()
            preferencesManager.setNotificationRequestCount(currentCount + 1)

            if (status == PermissionStatus.GRANTED) {
                preferencesManager.setNotificationPermission(true)
            } else if (status == PermissionStatus.DENIED) {
                // If denied, we check if it was permanently denied by the OS
                // In a real app, you might want to check shouldShowRequestPermissionRationale here
                // but we rely on the count as well.
                if (currentCount >= 2) {
                    updateState { copy(showPermanentlyDeniedDialog = true) }
                }
            }
        }
    }

    private fun checkPermission() {
        executeAsync {
            val grantedInPrefs = preferencesManager.notificationPermission.first()
            if (!grantedInPrefs) {
                val status = checkNotificationPermissionStatus()
                if (status != PermissionStatus.GRANTED) {
                    val requestCount = preferencesManager.notificationRequestCount.first()
                    if (requestCount < 2) {
                        updateState { copy(showNotificationPermissionDialog = true) }
                    } else if (requestCount == 2) {
                        // 3rd time - show explanation that leads to settings
                        updateState { copy(showNotificationPermissionDialog = true) }
                    } else {
                        // More than 3 requests or permanently denied - show settings dialog
                        updateState { copy(showPermanentlyDeniedDialog = true) }
                    }
                } else {
                    preferencesManager.setNotificationPermission(true)
                }
            }
        }
    }

    private fun loadUser() {
        val currentUser = userSession.user.value
        if (currentUser != null) {
            val role = userSession.role.value
            updateState { 
                copy(
                    user = currentUser,
                    isAdmin = role == UserRole.SUPER_ADMIN || role == UserRole.DISTRICT_ADMIN,
                    isOwner = role == UserRole.OWNER,
                    userRole = role,
                    isLoadingUser = false
                ) 
            }
            loadDashboardStats()
            return
        }

        updateState { copy(isLoadingUser = true) }
        executeAsync {
            val userId = preferencesManager.userId.filter { it != 0 }.first()
            getUserUseCase(userId.toLong()).collect { result ->
                userSession.setUser(result)
                val userRole = userSession.role.value

                updateState { 
                    copy(
                        user = result,
                        isAdmin = userRole == UserRole.SUPER_ADMIN || userRole == UserRole.DISTRICT_ADMIN,
                        isOwner = userRole == UserRole.OWNER,
                        userRole = userRole,
                        isLoadingUser = false
                    ) 
                }
                loadDashboardStats() // Load stats after role is determined
            }
        }
    }

    private fun loadSlots(id: Int, date: String, duration: String) {
        updateState { copy(isLoadingSlots = true, selectedSlot = null) }
        executeAsync {
            getStadiumByIdUseCase(id, date, duration).collect { responseList ->
                val stadium = responseList.firstOrNull()
                val triples = stadium?.slots?.map {
                    Triple(
                        LocalDateTime.parse(it.start ?: ""),
                        LocalDateTime.parse(it.end ?: ""),
                        it.status == "AVAILABLE"
                    )
                }?:emptyList()
                updateState { copy(stadiumSlots = triples, isLoadingSlots = false) }
            }
        }
    }

    private fun loadDashboardStats() {
        if (state.value.isAdmin) {
            updateState { copy(isLoadingDashboard = true, isLoadingWeeklyReport = true) }
            executeAsync {
                dashboardUseCase().collect { dashboard ->
                    val totalRev = dashboard.stadiumRevenues.sumOf { it.totalRevenue }
                    updateState {
                        copy(
                            dashboard = dashboard,
                            isLoadingDashboard = false,
                            activeStadiums = dashboard.activeStadiumsCount,
                            totalTournaments = dashboard.tournamentsCount,
                            totalUsers = dashboard.usersCount,
                            totalEarnings = totalRev
                        )
                    }
                }
            }
            executeAsync {
                weeklyReportUseCase().collect { report ->
                    updateState {
                        copy(
                            weeklyReport = report,
                            isLoadingWeeklyReport = false,
                            weeklyEarnings = report.dailyRevenue.map { it.revenue },
                            weeklyLabels = report.dailyRevenue.map { day ->
                                val parts = day.date.split("-")
                                if (parts.size == 3) "${parts[2]}.${parts[1]}" else day.date
                            }
                        )
                    }
                }
            }
        } else {
            // For non-admin roles, we don't call admin dashboard APIs.
            // Stats will be updated locally from other API calls (loadStadiums, loadMatches, etc.)
            updateState { 
                copy(
                    isLoadingDashboard = false, 
                    isLoadingWeeklyReport = false,
                    dashboard = null,
                    weeklyReport = null
                ) 
            }
            updateLocalStats()
        }
    }

    private fun updateLocalStats() {
        val s = state.value
        if (s.isAdmin) return // Admin uses dashboard API instead

        val totalEarnings = s.matches.sumOf { (it.currentPlayers ?: 0) * (it.pricePerPlayer ?: 0.0) }
        
        updateState { 
            copy(
                totalEarnings = totalEarnings,
                totalMatches = s.matches.size,
                totalTournaments = s.tournaments.size,
                // activeStadiums is updated in loadStadiums via pageData.totalElements
            ) 
        }
    }

    private fun loadStadiums(page: Int) {
        val s = state.value
        updateState { copy(isLoadingStadiums = true, stadiumError = null) }
        executeAsync(
            block = {
                var result = uz.coder.foottopbusiness.data.network.dto.stadium.PageStadiumResponseDto()
                getStadiumsUseCase(
                    name = s.searchQuery.takeIf { it.isNotBlank() },
                    isActive = s.filterActive,
                    page = page,
                ).collect { result = it }
                result
            },
            onSuccess = { pageData ->
                val newItems = pageData.content ?: emptyList()
                updateState {
                    copy(
                        stadiums = newItems,
                        currentPage = page,
                        isLastPage = pageData.last ?: true,
                        isLoadingStadiums = false,
                        activeStadiums = pageData.totalElements?.toInt() ?: activeStadiums
                    )
                }
            },
            onError = { updateState { copy(isLoadingStadiums = false, stadiumError = it.message) } }
        )
    }

    private fun loadCoaches() {
        updateState { copy(isLoadingCoaches = true) }
        executeAsync(
            block = {
                var r = emptyList<uz.coder.foottopbusiness.data.network.dto.CoachResponseDto>()
                getCoachesUseCase().collect { r = it }
                r
            },
            onSuccess = { updateState { copy(coaches = it, isLoadingCoaches = false) } },
            onError = { updateState { copy(isLoadingCoaches = false) } }
        )
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync {
            getMatchesUseCase().collect { matches ->
                val latest = matches.sortedByDescending { it.dateTime }.take(10)
                updateState { copy(matches = matches, latestMatches = latest, isLoadingMatches = false) }
                updateLocalStats()
            }
        }
    }

    private fun loadTournaments() {
        updateState { copy(isLoadingTournaments = true) }
        executeAsync {
            getTournamentsUseCase().collect { pageData ->
                updateState { copy(tournaments = pageData.content ?: emptyList(), isLoadingTournaments = false) }
                updateLocalStats()
            }
        }
    }
}

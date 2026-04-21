package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.zip
import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.presentation.main.home.HomeContract.Effect.*

class HomeViewModel(
    private val getStadiumsUseCase: GetStadiumsUseCase,
    private val deleteStadiumUseCase: DeleteStadiumUseCase,
    private val getTournamentsUseCase: GetTournamentsUseCase,
    private val getMatchesUseCase: GetMatchesUseCase,
    private val updateOpenCloseTimeUseCase: UpdateOpenCloseTimeUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getStadiumByIdUseCase: GetStadiumByIdUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val preferencesManager: PreferencesManager,
    private val getCoachesUseCase: uz.coder.foottopbusiness.domain.usecase.coach.GetCoachesUseCase,
) : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    initialState = HomeContract.State(
        selectedDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    )
) {
    init {
        handleEvent(HomeContract.Event.Load)
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        // This is a placeholder for checking permission.
        // In a real app, you would use a platform-specific check.
        // For now, we'll assume we need to ask if the user is an owner/admin
        // and show the dialog once.
        executeAsync {
            // Simulate checking if we've already asked or if permission is granted
            val alreadyAsked = preferencesManager.userId.first() != 0 // Just a dummy condition
            if (!alreadyAsked) {
                updateState { copy(showNotificationPermissionDialog = true) }
            }
        }
    }

    override fun handleEvent(event: HomeContract.Event) {
        when (event) {
            HomeContract.Event.Load, HomeContract.Event.Refresh -> {
                updateState { copy(currentPage = 0, stadiums = emptyList(), isLastPage = false) }
                loadUser()
                loadStadiums(0)
                loadTournaments()
                loadMatches()
                loadDashboardStats()
                loadCoaches()
            }

            is HomeContract.Event.ChangeTab -> updateState { copy(currentTab = event.index) }
            
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
                val duration = state.value.selectedDuration
                val slotIndex = state.value.stadiumSlots.indexOf(event.slot)
                if (slotIndex == -1) return

                val slotsToSelect = when (duration) {
                    "SIXTY" -> 3
                    "NINETY" -> 4
                    "HUNDRED_TWENTY" -> 5
                    else -> 1
                }

                val availableSlots = state.value.stadiumSlots
                val canBook = if (slotIndex + slotsToSelect <= availableSlots.size) {
                    (slotIndex until slotIndex + slotsToSelect).all { availableSlots[it].third }
                } else {
                    false
                }

                if (canBook) {
                    updateState { copy(selectedSlot = event.slot, isBookingSlot = true) }
                } else {
                    sendEffect(HomeContract.Effect.ShowToast("Tanlangan vaqt oralig'ida bo'sh joy yetarli emas"))
                }
            }

            is HomeContract.Event.CreateBooking -> {
                updateState { copy(isBookingSlot = false, selectedSlot = null) }
                sendEffect(ShowToast("Muvaffaqiyatli band qilindi: ${event.name}"))
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
                    sendEffect(HomeContract.Effect.DownloadFile("hisobot_${state.value.selectedDate}.csv", csv.toString()))
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
                updateState { copy(showNotificationPermissionDialog = false) }
                // Since this is KMP, the actual permission request will be handled in the UI layer 
                // or via a platform-specific side effect.
            }
        }
    }

    private fun loadUser() {
        updateState { copy(isLoadingUser = true) }
        executeAsync {
            val userId = preferencesManager.userId.filter { it != 0 }.first()
            getUserUseCase(userId.toLong()).collect { result ->
                val isSuperAdmin = result.roles?.any { it.name == "ROLE_SUPER_ADMIN" || it.name == "SUPER_ADMIN" } ?: false
                val isDistrictAdmin = result.roles?.any { it.name == "ROLE_DISTRICT_ADMIN" } ?: false
                val isOwner = result.roles?.any { it.name == "ROLE_OWNER" } ?: false
                
                val userRole = when {
                    isSuperAdmin -> UserRole.SUPER_ADMIN
                    isDistrictAdmin -> UserRole.DISTRICT_ADMIN
                    isOwner -> UserRole.OWNER
                    result.roles?.any { it.name?.contains("COACH", ignoreCase = true) == true || it.name?.contains("MURABBIY", ignoreCase = true) == true } == true -> UserRole.COACH
                    else -> UserRole.fromString(result.roles?.firstOrNull()?.name)
                }

                updateState { 
                    copy(
                        user = result,
                        isAdmin = isSuperAdmin || isDistrictAdmin,
                        isOwner = isOwner,
                        userRole = userRole,
                        isLoadingUser = false
                    ) 
                }
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
        executeAsync {
            val stadiumsFlow = getStadiumsUseCase(isActive = null)
            val tournamentsFlow = getTournamentsUseCase()
            val matchesFlow = getMatchesUseCase()
            val usersFlow = getAllUsersUseCase()

            stadiumsFlow.zip(tournamentsFlow) { s, t -> s to t }
                .zip(matchesFlow) { (s, t), m -> Triple(s, t, m) }
                .zip(usersFlow) { (s, t, m), u ->
                    val totalEarnings = m.sumOf { (it.currentPlayers ?: 0) * (it.pricePerPlayer ?: 0.0) }

                    val weeklyLabels = mutableListOf<String>()
                    val weeklyEarnings = mutableListOf<Double>()

                    for (i in 6 downTo 0) {
                        val day = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.minus(i, DateTimeUnit.DAY)
                        val dayStr = day.toString() // YYYY-MM-DD
                        val dayEarnings = m.filter { it.dateTime?.startsWith(dayStr) == true }
                            .sumOf { (it.currentPlayers?.toDouble() ?: 0.0) * (it.pricePerPlayer ?: 0.0) }

                        weeklyEarnings.add(dayEarnings)
                        weeklyLabels.add(
                            when (day.dayOfWeek) {
                                DayOfWeek.MONDAY -> "Du"
                                DayOfWeek.TUESDAY -> "Se"
                                DayOfWeek.WEDNESDAY -> "Ch"
                                DayOfWeek.THURSDAY -> "Pa"
                                DayOfWeek.FRIDAY -> "Ju"
                                DayOfWeek.SATURDAY -> "Sh"
                                DayOfWeek.SUNDAY -> "Ya"
                            }
                        )
                    }

                    updateState {
                        copy(
                            activeStadiums = s.content?.count { it.isActive == true } ?: 0,
                            totalTournaments = t.size,
                            totalMatches = m.size,
                            totalEarnings = totalEarnings,
                            totalUsers = u.size,
                            weeklyEarnings = weeklyEarnings,
                            weeklyLabels = weeklyLabels
                        )
                    }
                }.collect {}
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
                    )
                }
            },
            onError = { updateState { copy(isLoadingStadiums = false, stadiumError = it.message) } }
        )
    }

    private fun loadTournaments() {
        updateState { copy(isLoadingTournaments = true) }
        executeAsync(
            block = {
                var r = emptyList<TournamentResponseDto>()
                getTournamentsUseCase().collect { r = it }
                r
            },
            onSuccess = { updateState { copy(tournaments = it, isLoadingTournaments = false) } },
            onError = { updateState { copy(isLoadingTournaments = false) } }
        )
    }

    private fun loadMatches() {
        updateState { copy(isLoadingMatches = true) }
        executeAsync(
            block = {
                var r = emptyList<MatchResponseDto>()
                getMatchesUseCase().collect { r = it }
                r
            },
            onSuccess = { updateState { copy(matches = it, isLoadingMatches = false) } },
            onError = { updateState { copy(isLoadingMatches = false) } }
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
}

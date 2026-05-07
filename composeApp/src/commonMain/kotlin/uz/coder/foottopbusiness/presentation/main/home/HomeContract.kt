package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.Dashboard
import uz.coder.foottopbusiness.domain.model.UserRole
import uz.coder.foottopbusiness.domain.model.WeeklyReport

sealed interface HomeContract {
    data class State(
        // Navigation
        val currentTab: Int = 0, // 0: Home, 1: History, 2: Profile
        val showStadiumTable: Boolean = false,
        
        // Role info
        val isAdmin: Boolean = false,
        val isOwner: Boolean = false,
        val userRole: UserRole = UserRole.UNKNOWN,
        
        // User info
        val user: UserDto? = null,
        val isLoadingUser: Boolean = false,
        
        // dashboard stats (Domain Models)
        val dashboard: Dashboard? = null,
        val weeklyReport: WeeklyReport? = null,
        val isLoadingDashboard: Boolean = false,
        val isLoadingWeeklyReport: Boolean = false,
        
        // dashboard stats (legacy/calculated)
        val totalEarnings: Double = 0.0,
        val activeStadiums: Int = 0,
        val totalTournaments: Int = 0,
        val totalMatches: Int = 0,
        val totalUsers: Int = 0,
        
        // charts
        val weeklyEarnings: List<Double> = emptyList(),
        val weeklyLabels: List<String> = emptyList(),
        
        // stadiums & slots
        val stadiums: List<StadiumResponse> = emptyList(),
        val isLoadingStadiums: Boolean = false,
        val stadiumError: String? = null,
        val searchQuery: String = "",
        val filterActive: Boolean? = null,
        val currentPage: Int = 0,
        val isLastPage: Boolean = false,
        val deletingId: Int? = null,
        
        // Time & Slot control
        val selectedStadiumForTime: StadiumResponse? = null,
        val isUpdatingTime: Boolean = false,
        val selectedDate: String = "", // YYYY-MM-DD
        val selectedDuration: String = "SIXTY", // SIXTY, NINETY, ONE_HUNDRED_TWENTY
        val stadiumSlots: List<Triple<LocalDateTime, LocalDateTime, Boolean>> = emptyList(),
        val isLoadingSlots: Boolean = false,
        val newOpenTime: String = "",
        val newCloseTime: String = "",
        val selectedSlot: Triple<LocalDateTime, LocalDateTime, Boolean>? = null,
        val isBookingSlot: Boolean = false,
        
        // tournaments
        val tournaments: List<TournamentResponseDto> = emptyList(),
        val isLoadingTournaments: Boolean = false,
        val selectedTournament: TournamentResponseDto? = null,
        
        // matches
        val matches: List<MatchResponseDto> = emptyList(),
        val isLoadingMatches: Boolean = false,
        val selectedMatch: MatchResponseDto? = null,
        
        // Dynamic home data
        val coaches: List<uz.coder.foottopbusiness.data.network.dto.CoachResponseDto> = emptyList(),
        val isLoadingCoaches: Boolean = false,
        
        // Notification permission
        val showNotificationPermissionDialog: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        data class DownloadFile(val fileName: String, val content: String) : Effect
        object Stadium: Effect
        object Match: Effect
        object Tournament: Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Refresh : Event
        data class ChangeTab(val index: Int) : Event
        data class SetShowStadiumTable(val show: Boolean) : Event
        data class Search(val query: String) : Event
        data class FilterActive(val isActive: Boolean?) : Event
        object LoadNextPage : Event
        object LoadPreviousPage : Event
        data class LoadPage(val page: Int) : Event
        data class DeleteRequest(val id: Int) : Event
        object DeleteConfirm : Event
        object DeleteCancel : Event
        
        // Slot & Time control events
        data class SelectStadiumForSlots(val stadium: StadiumResponse) : Event
        data class ChangeDate(val date: String) : Event
        data class ChangeDuration(val duration: String) : Event
        data class SelectSlot(val slot: Triple<LocalDateTime, LocalDateTime, Boolean>) : Event
        data class CreateBooking(val name: String, val phone: String) : Event
        object DismissBookingDialog : Event
        object ClearStadiumForSlots : Event
        data class UpdateTime(val open: String, val close: String) : Event
        
        data class SelectTournament(val t: TournamentResponseDto) : Event
        object ClearTournament : Event
        data class SelectMatch(val m: MatchResponseDto) : Event
        object ClearMatch : Event
        
        object DownloadReport : Event
        object Logout : Event
        object Stadium: Event
        object Match: Event
        object Tournament: Event
        object ShowExitToast : Event
        
        data class SetShowNotificationPermissionDialog(val show: Boolean) : Event
        object RequestNotificationPermission : Event
    }
}

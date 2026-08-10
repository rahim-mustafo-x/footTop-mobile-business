package uz.coder.foottopbusiness.presentation.main.home

import kotlinx.datetime.LocalDateTime
import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.core.platform.PermissionStatus
import uz.coder.foottopbusiness.data.network.dto.MatchResponseDto
import uz.coder.foottopbusiness.data.network.dto.TournamentResponseDto
import uz.coder.foottopbusiness.data.network.dto.UserDto
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.model.UserRole

sealed interface HomeContract {
    data class State(
        // Rol ma'lumoti
        val isAdmin: Boolean = false,
        val isOwner: Boolean = false,
        val userRole: UserRole = UserRole.UNKNOWN,

        // Foydalanuvchi
        val user: UserDto? = null,
        val isLoadingUser: Boolean = false,

        // Bosh sahifadagi statistika kartalari.
        // Dashboard'ning xom obyekti bu yerda saqlanmaydi - u faqat
        // ReportsScreen'ga kerak, u esa o'z ReportsViewModel'idan oladi.
        val totalEarnings: Double = 0.0,
        val activeStadiums: Int = 0,
        val totalTournaments: Int = 0,
        val totalUsers: Int = 0,

        // Bron qilishda stadion tanlash ro'yxati. Statistika so'rovi allaqachon
        // birinchi sahifani olib keladi - shu ma'lumotni tashlab yubormaymiz.
        val stadiums: List<StadiumResponse> = emptyList(),

        // Slot boshqaruvi - SlotsControlScreen o'qiydi
        val selectedStadiumForTime: StadiumResponse? = null,
        val selectedDate: String = "", // YYYY-MM-DD
        val selectedDuration: String = "SIXTY", // SIXTY, NINETY, ONE_HUNDRED_TWENTY
        val stadiumSlots: List<Triple<LocalDateTime, LocalDateTime, Boolean>> = emptyList(),
        // Haqiqatda bron bilan band bo'lgan slotlar boshlanish vaqti. Qolgan bo'sh
        // emas slotlar - vaqti bo'sh, lekin tanlangan davomiylik sig'maydiganlari.
        val occupiedSlotStarts: Set<LocalDateTime> = emptySet(),
        val isLoadingSlots: Boolean = false,
        val selectedSlot: Triple<LocalDateTime, LocalDateTime, Boolean>? = null,
        val isBookingSlot: Boolean = false,

        // Turnirlar
        val tournaments: List<TournamentResponseDto> = emptyList(),
        val isLoadingTournaments: Boolean = false,
        val selectedTournament: TournamentResponseDto? = null,

        // O'yinlar
        val matches: List<MatchResponseDto> = emptyList(),
        val isLoadingMatches: Boolean = false,

        // Bildirishnoma ruxsati
        val showNotificationPermissionDialog: Boolean = false,
        val showPermanentlyDeniedDialog: Boolean = false,
        val triggerNotificationRequest: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
        object NavigateBack : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        object Refresh : Event

        // Slot va band qilish
        data class SelectStadiumForSlots(val stadium: StadiumResponse) : Event
        data class ChangeDate(val date: String) : Event
        data class ChangeDuration(val duration: String) : Event
        data class SelectSlot(val slot: Triple<LocalDateTime, LocalDateTime, Boolean>) : Event
        data class CreateBooking(val name: String, val phone: String) : Event
        object DismissBookingDialog : Event
        object ClearStadiumForSlots : Event

        data class SelectTournament(val t: TournamentResponseDto) : Event
        object ClearTournament : Event

        object ShowExitToast : Event

        // Bildirishnoma ruxsati
        data class SetShowNotificationPermissionDialog(val show: Boolean) : Event
        object RequestNotificationPermission : Event
        object CheckNotificationPermission : Event
        object DismissPermanentlyDeniedDialog : Event
        object OpenSettings : Event
        data class OnNotificationPermissionResult(val status: PermissionStatus) : Event
    }
}

package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.StadiumTab

enum class StadiumType(val label: String) {
    FOOTBALL("Football"),
    BASKETBALL("Basketball"),
    TENNIS("Tennis"),
    VOLLEYBALL("Volleyball"),
}

enum class StadiumDuration(val label: String) {
    SIXTY("60 min"),
    NINETY("90 min"),
    ONE_TWENTY("120 min"),
}

sealed interface StadiumContract {
    data class State(
        val selectedTab: StadiumTab = StadiumTab.Details,
        val stadiumName: String = "",
        val description: String = "",
        val type: StadiumType = StadiumType.FOOTBALL,
        val duration: StadiumDuration = StadiumDuration.SIXTY,
        val capacity: String = "",
        val pricePerHour: String = "",
        val openingTime: String = "",
        val closingTime: String = "",
        val upfrontEnabled: Boolean = false,
        val splitPaymentEnabled: Boolean = false,
        val showTypeDropdown: Boolean = false,
        val showDurationDropdown: Boolean = false,
        val isLoading: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect {
        data class ShowToast(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        data class SelectTab(val tab: StadiumTab) : Event
        data class StadiumName(val value: String) : Event
        data class Description(val value: String) : Event
        data class Type(val value: StadiumType) : Event
        data class Duration(val value: StadiumDuration) : Event
        data class Capacity(val value: String) : Event
        data class PricePerHour(val value: String) : Event
        data class OpeningTime(val value: String) : Event
        data class ClosingTime(val value: String) : Event
        data class UpfrontEnabled(val value: Boolean) : Event
        data class SplitPaymentEnabled(val value: Boolean) : Event
        data class ShowTypeDropdown(val show: Boolean) : Event
        data class ShowDurationDropdown(val show: Boolean) : Event
        object Save : Event
    }
}

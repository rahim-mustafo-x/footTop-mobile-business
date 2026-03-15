package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.presentation.main.stadium.tabs.StadiumTab

sealed interface StadiumContract {
    data class State(
        val selectedTab: StadiumTab = StadiumTab.Details,
        val stadiumName: String = "",
        val openingTime: String = "",
        val closingTime: String = "",
        val upfrontEnabled: Boolean = false,
        val splitPaymentEnabled: Boolean = false,
    ) : MviState

    sealed interface Effect : MviEffect
    sealed interface Event : MviEvent {
        data class SelectTab(val tab: StadiumTab) : Event
        data class StadiumName(val value: String) : Event
        data class OpeningTime(val value: String) : Event
        data class ClosingTime(val value: String) : Event
        data class UpfrontEnabled(val value: Boolean) : Event
        data class SplitPaymentEnabled(val value: Boolean) : Event
    }
}

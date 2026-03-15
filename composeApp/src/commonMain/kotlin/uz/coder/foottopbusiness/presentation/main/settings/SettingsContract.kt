package uz.coder.foottopbusiness.presentation.main.settings

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface SettingsContract {
    data class State(val dummy: Unit = Unit) : MviState

    sealed interface Effect : MviEffect {
        object NavigateToAuth : Effect
    }

    sealed interface Event : MviEvent {
        object Logout : Event
    }
}

package uz.coder.foottopbusiness.presentation.main.coaches

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface CoachesContract {
    data class State(val dummy: Unit = Unit) : MviState
    sealed interface Effect : MviEffect
    sealed interface Event : MviEvent
}

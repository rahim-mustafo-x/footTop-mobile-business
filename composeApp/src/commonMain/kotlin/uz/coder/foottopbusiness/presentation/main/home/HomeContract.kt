package uz.coder.foottopbusiness.presentation.main.home

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState

sealed interface HomeContract {
    data class State(val dummy: Unit = Unit) : MviState
    sealed interface Effect : MviEffect
    sealed interface Event : MviEvent
}

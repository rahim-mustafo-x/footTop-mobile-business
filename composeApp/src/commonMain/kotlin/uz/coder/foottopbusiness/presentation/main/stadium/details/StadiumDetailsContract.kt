package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse

sealed interface StadiumDetailsContract {
    data class State(
        val stadium: StadiumResponse? = null,
        val isLoading: Boolean = false
    ) : MviState

    sealed interface Effect : MviEffect {
        object NavigateBack : Effect
    }

    sealed interface Event : MviEvent {
        object BackClick : Event
    }
}

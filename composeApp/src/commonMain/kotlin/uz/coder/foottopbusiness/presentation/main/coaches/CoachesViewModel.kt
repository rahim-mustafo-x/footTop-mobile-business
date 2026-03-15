package uz.coder.foottopbusiness.presentation.main.coaches

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class CoachesViewModel : BaseViewModel<CoachesContract.State, CoachesContract.Effect, CoachesContract.Event>(
    initialState = CoachesContract.State()
) {
    override fun handleEvent(event: CoachesContract.Event) {}
}

package uz.coder.foottopbusiness.presentation.main.tournaments

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class TournamentsViewModel : BaseViewModel<TournamentsContract.State, TournamentsContract.Effect, TournamentsContract.Event>(
    initialState = TournamentsContract.State()
) {
    override fun handleEvent(event: TournamentsContract.Event) {}
}

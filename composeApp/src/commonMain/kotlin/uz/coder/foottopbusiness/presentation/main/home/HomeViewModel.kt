package uz.coder.foottopbusiness.presentation.main.home

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class HomeViewModel : BaseViewModel<HomeContract.State, HomeContract.Effect, HomeContract.Event>(
    initialState = HomeContract.State()
) {
    override fun handleEvent(event: HomeContract.Event) {}
}

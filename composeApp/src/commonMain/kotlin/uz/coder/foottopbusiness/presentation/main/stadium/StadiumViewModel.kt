package uz.coder.foottopbusiness.presentation.main.stadium

import uz.coder.foottopbusiness.core.mvi.BaseViewModel

class StadiumViewModel : BaseViewModel<StadiumContract.State, StadiumContract.Effect, StadiumContract.Event>(
    initialState = StadiumContract.State()
) {
    override fun handleEvent(event: StadiumContract.Event) {
        when (event) {
            is StadiumContract.Event.SelectTab -> updateState { copy(selectedTab = event.tab) }
            is StadiumContract.Event.StadiumName -> updateState { copy(stadiumName = event.value) }
            is StadiumContract.Event.OpeningTime -> updateState { copy(openingTime = event.value) }
            is StadiumContract.Event.ClosingTime -> updateState { copy(closingTime = event.value) }
            is StadiumContract.Event.UpfrontEnabled -> updateState { copy(upfrontEnabled = event.value) }
            is StadiumContract.Event.SplitPaymentEnabled -> updateState { copy(splitPaymentEnabled = event.value) }
        }
    }
}

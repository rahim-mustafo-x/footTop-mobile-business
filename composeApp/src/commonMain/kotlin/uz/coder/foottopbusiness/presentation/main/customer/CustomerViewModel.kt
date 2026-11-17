package uz.coder.foottopbusiness.presentation.main.customer

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.booking.GetCustomerSummaryUseCase

class CustomerViewModel(
    phone: String,
    private val getCustomerSummaryUseCase: GetCustomerSummaryUseCase
) : BaseViewModel<CustomerContract.State, CustomerContract.Effect, CustomerContract.Event>(
    initialState = CustomerContract.State(phone = phone)
) {
    init {
        load()
    }

    override fun handleEvent(event: CustomerContract.Event) {
        when (event) {
            CustomerContract.Event.Load -> load()
        }
    }

    private fun load() {
        executeAsync(
            onLoading = { updateState { copy(isLoading = true, error = null) } },
            block = { getCustomerSummaryUseCase(state.value.phone).first() },
            onSuccess = { summary -> updateState { copy(isLoading = false, summary = summary) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message.orEmpty()) } }
        )
    }
}

package uz.coder.foottopbusiness.presentation.main.stadium

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.domain.usecase.stadium.CreateStadiumUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedDistrictIdUseCase
import uz.coder.foottopbusiness.domain.usecase.stadium.GetSavedRegionIdUseCase

class StadiumViewModel(
    private val createStadiumUseCase: CreateStadiumUseCase,
    private val getSavedRegionIdUseCase: GetSavedRegionIdUseCase,
    private val getSavedDistrictIdUseCase: GetSavedDistrictIdUseCase,
) : BaseViewModel<StadiumContract.State, StadiumContract.Effect, StadiumContract.Event>(
    initialState = StadiumContract.State()
) {
    override fun handleEvent(event: StadiumContract.Event) {
        when (event) {
            is StadiumContract.Event.SelectTab -> updateState { copy(selectedTab = event.tab) }
            is StadiumContract.Event.StadiumName -> updateState { copy(stadiumName = event.value) }
            is StadiumContract.Event.Description -> updateState { copy(description = event.value) }
            is StadiumContract.Event.Type -> updateState { copy(type = event.value, showTypeDropdown = false) }
            is StadiumContract.Event.Duration -> updateState { copy(duration = event.value, showDurationDropdown = false) }
            is StadiumContract.Event.Capacity -> updateState { copy(capacity = event.value) }
            is StadiumContract.Event.PricePerHour -> updateState { copy(pricePerHour = event.value) }
            is StadiumContract.Event.OpeningTime -> updateState { copy(openingTime = event.value) }
            is StadiumContract.Event.ClosingTime -> updateState { copy(closingTime = event.value) }
            is StadiumContract.Event.UpfrontEnabled -> updateState { copy(upfrontEnabled = event.value) }
            is StadiumContract.Event.SplitPaymentEnabled -> updateState { copy(splitPaymentEnabled = event.value) }
            is StadiumContract.Event.ShowTypeDropdown -> updateState { copy(showTypeDropdown = event.show) }
            is StadiumContract.Event.ShowDurationDropdown -> updateState { copy(showDurationDropdown = event.show) }
            is StadiumContract.Event.Save -> save()
        }
    }

    private fun save() {
        val s = state.value
        executeAsync(
            onLoading = { updateState { copy(isLoading = true) } },
            onError = { e ->
                updateState { copy(isLoading = false) }
                sendEffect(StadiumContract.Effect.ShowToast(e.message ?: "Xatolik yuz berdi"))
            },
            block = {
                // Fetch IDs from preferences via UseCases, bypassing presentation state
                val regionId = getSavedRegionIdUseCase().first()
                val districtId = getSavedDistrictIdUseCase().first()

                createStadiumUseCase(
                    name = s.stadiumName,
                    description = s.description,
                    type = s.type.name,
                    duration = s.duration.name,
                    capacity = s.capacity.toIntOrNull() ?: 0,
                    pricePerHour = s.pricePerHour.toIntOrNull() ?: 0,
                    openTime = s.openingTime,
                    closeTime = s.closingTime,
                    imageUrl = "",
                    regionId = regionId,
                    districtId = districtId,
                ).first()
            },
            onSuccess = {
                updateState { copy(isLoading = false) }
                sendEffect(StadiumContract.Effect.ShowToast("Muvaffaqiyatli saqlandi"))
            }
        )
    }
}

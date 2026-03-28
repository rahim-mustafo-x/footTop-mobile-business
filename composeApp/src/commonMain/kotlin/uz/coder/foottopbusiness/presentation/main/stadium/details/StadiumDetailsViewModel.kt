package uz.coder.foottopbusiness.presentation.main.stadium.details

import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.data.network.dto.stadium.StadiumResponse
import uz.coder.foottopbusiness.domain.repository.StadiumRepository
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class StadiumDetailsViewModel(
    stadium: StadiumResponse,
    private val stadiumRepository: StadiumRepository
) : BaseViewModel<StadiumDetailsContract.State, StadiumDetailsContract.Effect, StadiumDetailsContract.Event>(
    initialState = StadiumDetailsContract.State(stadium = stadium)
) {
    override fun handleEvent(event: StadiumDetailsContract.Event) {
        when (event) {
            StadiumDetailsContract.Event.BackClick -> sendEffect(StadiumDetailsContract.Effect.NavigateBack)
            StadiumDetailsContract.Event.EditClick -> {
                state.value.stadium?.let {
                    sendEffect(StadiumDetailsContract.Effect.NavigateToEdit(it))
                }
            }
            is StadiumDetailsContract.Event.ToggleActive -> {
                updateStatus(event.isActive)
            }
        }
    }

    private fun updateStatus(isActive: Boolean) {
        val currentStadium = state.value.stadium ?: return
        updateState { copy(isUpdatingStatus = true) }
        
        executeAsync {
            stadiumRepository.updateStadium(
                id = currentStadium.id ?: return@executeAsync,
                name = currentStadium.name ?: "",
                description = currentStadium.description ?: "",
                type = currentStadium.type ?: "",
                duration = currentStadium.duration ?: "",
                capacity = currentStadium.capacity ?: 0,
                pricePerHour = currentStadium.pricePerHour?.toInt() ?: 0,
                openTime = currentStadium.openTime ?: "",
                closeTime = currentStadium.closeTime ?: "",
                imageUrl = "", 
                regionId = 1, 
                districtId = 1,
                isActive = isActive
            ).collect { _ ->
                // After success, fetch the updated stadium data
                refreshStadium(currentStadium.id)
            }
        }
    }

    private fun refreshStadium(id: Int) {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${now.year}-${now.month.number.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        
        executeAsync {
            stadiumRepository.getStadiumById(id, dateStr, "01:00:00").collect { stadiums ->
                val updated = stadiums.firstOrNull { it.id == id }
                updateState { copy(stadium = updated, isUpdatingStatus = false) }
                sendEffect(StadiumDetailsContract.Effect.ShowToast("Stadion holati yangilandi"))
            }
        }
    }
}

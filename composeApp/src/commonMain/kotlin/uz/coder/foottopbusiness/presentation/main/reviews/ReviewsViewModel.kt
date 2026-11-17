package uz.coder.foottopbusiness.presentation.main.reviews

import kotlinx.coroutines.flow.first
import uz.coder.foottopbusiness.core.mvi.BaseViewModel
import uz.coder.foottopbusiness.core.mvi.MviEffect
import uz.coder.foottopbusiness.core.mvi.MviEvent
import uz.coder.foottopbusiness.core.mvi.MviState
import uz.coder.foottopbusiness.data.network.dto.owner.StaffRatingDto
import uz.coder.foottopbusiness.domain.usecase.owner.GetStaffRatingsUseCase
import uz.coder.foottopbusiness.domain.usecase.owner.ReplyToRatingUseCase

/** Javob uzunligi chegarasi (backend RATING_REPLY_TOO_LONG). */
const val MAX_REPLY = 500

sealed interface ReviewsContract {
    data class State(
        val isLoading: Boolean = true,
        val ratings: List<StaffRatingDto> = emptyList(),
        val error: String? = null,
        val onlyUnanswered: Boolean = false,
        val editing: StaffRatingDto? = null,
        val replyText: String = "",
        val isSaving: Boolean = false
    ) : MviState {
        val unanswered: List<StaffRatingDto> get() = ratings.filter { !it.comment.isNullOrBlank() && it.ownerReply.isNullOrBlank() }
        val shown: List<StaffRatingDto> get() = if (onlyUnanswered) unanswered else ratings
        val average: Double? get() = if (ratings.isEmpty()) null else ratings.sumOf { it.rating ?: 0 }.toDouble() / ratings.size
    }

    sealed interface Effect : MviEffect {
        object ReplySaved : Effect
        object ReplyDeleted : Effect
        data class ShowError(val message: String) : Effect
    }

    sealed interface Event : MviEvent {
        object Load : Event
        data class SetOnlyUnanswered(val value: Boolean) : Event
        data class Edit(val rating: StaffRatingDto) : Event
        object DismissEdit : Event
        data class UpdateReply(val text: String) : Event
        object SaveReply : Event
        object DeleteReply : Event
    }
}

/** O'yinchilarning egasi stadionlari haqidagi baholari va sharhlari, egasining javoblari bilan. */
class ReviewsViewModel(
    private val getStaffRatingsUseCase: GetStaffRatingsUseCase,
    private val replyToRatingUseCase: ReplyToRatingUseCase
) : BaseViewModel<ReviewsContract.State, ReviewsContract.Effect, ReviewsContract.Event>(
    initialState = ReviewsContract.State()
) {
    init {
        load()
    }

    override fun handleEvent(event: ReviewsContract.Event) {
        when (event) {
            ReviewsContract.Event.Load -> load()
            is ReviewsContract.Event.SetOnlyUnanswered -> updateState { copy(onlyUnanswered = event.value) }
            is ReviewsContract.Event.Edit -> updateState { copy(editing = event.rating, replyText = event.rating.ownerReply.orEmpty()) }
            ReviewsContract.Event.DismissEdit -> updateState { copy(editing = null) }
            is ReviewsContract.Event.UpdateReply -> updateState { copy(replyText = event.text) }
            ReviewsContract.Event.SaveReply -> save(state.value.replyText.trim())
            ReviewsContract.Event.DeleteReply -> save("")
        }
    }

    private fun load() {
        executeAsync(
            onLoading = { updateState { copy(isLoading = true, error = null) } },
            block = { getStaffRatingsUseCase().first() },
            onSuccess = { list -> updateState { copy(isLoading = false, ratings = list) } },
            onError = { e -> updateState { copy(isLoading = false, error = e.message.orEmpty()) } }
        )
    }

    private fun save(text: String) {
        val s = state.value
        val rating = s.editing ?: return
        if (s.isSaving || text.length > MAX_REPLY) return
        executeAsync(
            onLoading = { updateState { copy(isSaving = true) } },
            block = { replyToRatingUseCase(rating.id, text).first() },
            onSuccess = { updated ->
                updateState {
                    copy(
                        isSaving = false,
                        editing = null,
                        ratings = ratings.map { if (it.id == updated.id) updated else it }
                    )
                }
                sendEffect(if (text.isEmpty()) ReviewsContract.Effect.ReplyDeleted else ReviewsContract.Effect.ReplySaved)
            },
            onError = { e ->
                updateState { copy(isSaving = false) }
                sendEffect(ReviewsContract.Effect.ShowError(e.message.orEmpty()))
            }
        )
    }
}

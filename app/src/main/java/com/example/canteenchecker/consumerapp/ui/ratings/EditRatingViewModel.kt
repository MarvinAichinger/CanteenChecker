package com.example.canteenchecker.consumerapp.ui.ratings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteenchecker.consumerapp.logic.RatingApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class EditRatingViewModel internal constructor(
    private val canteenId: String,
    private val ratingApi: RatingApi,
) : ViewModel() {
    sealed interface UI {
        data object Loading : UI
        data object Error : UI
        data object Done : UI
        data class Input(
            val comment: String,
            val stars: Int,
            val hasFailed: Boolean = false,
        ) : UI
    }

    var ui: UI by mutableStateOf(UI.Loading)
        private set

    private var job: Job? = null

    init {
        load()
    }

    fun load() {
        ui = UI.Loading
        job?.cancel()
        job = viewModelScope.launch {
            ratingApi.getRating(canteenId).onSuccess { rating ->
                ui = UI.Input(
                    comment = rating?.comment ?: "",
                    stars = rating?.stars ?: 0,
                )
            }.onFailure {
                ui = UI.Error
            }
        }
    }

    fun setComment(value: String) {
        withInputUI {
            ui = copy(
                comment = value,
            )
        }
    }

    fun setStars(value: Int) {
        withInputUI {
            ui = copy(
                stars = value,
            )
        }
    }

    fun save() {
        withInputUI {
            ui = UI.Loading
            job?.cancel()
            job = viewModelScope.launch {
                ratingApi.setRating(
                    canteenId = canteenId,
                    stars = stars,
                    comment = comment.ifBlank { null },
                ).onSuccess {
                    ui = UI.Done
                }.onFailure {
                    ui = copy(
                        hasFailed = true,
                    )
                }
            }
        }
    }

    private fun withInputUI(action: UI.Input.() -> Unit) {
        (ui as? UI.Input)?.run(action)
    }
}
package com.example.canteenchecker.consumerapp.ui.canteens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteenchecker.consumerapp.CanteenCheckerApplication
import com.example.canteenchecker.consumerapp.logic.Authentication
import com.example.canteenchecker.consumerapp.logic.Rating
import com.example.canteenchecker.consumerapp.logic.RatingApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class RatingViewModel(
    private val canteenId: String,
    private val ratingApi: RatingApi,
    private val authentication: Authentication,
): ViewModel() {

    internal sealed interface UI {
        data object Loading: UI
        data object Error: UI
        data object NotSignedIn: UI
        data object NotRated: UI
        data class Rated(
            val rating: Rating
        ): UI
    }

    var ui: UI by mutableStateOf(UI.Loading)
        private set

    private var job: Job? = null

    private fun withReload(block: suspend () -> Result<Unit>) {
        ui = UI.Loading
        job?.cancel()
        job = viewModelScope.launch {
            block()
                .onSuccess {
                    if (!authentication.authenticated()) {
                        ui = UI.NotSignedIn
                    } else {
                        ratingApi.getRating(canteenId)
                            .onSuccess { rating ->
                            ui = if (rating != null) UI.Rated(rating) else UI.NotRated
                        }
                        .onFailure {
                            ui = UI.Error
                        }
                    }
                }
                .onFailure {
                    ui = UI.Error
                }
        }
    }

    init {
        reload()
    }

    fun reload() {
        withReload { Result.success(Unit) }
    }

    fun signIn(activity: Activity) {
        withReload { Result.success(authentication.signIn(activity)) }
    }

    fun signOut() {
        withReload { Result.success(authentication.signOut()) }
    }

    fun deleteRating() {
        withReload { ratingApi.resetRating(canteenId) }
    }

}

@Composable
private fun ratingViewModel(canteenId: String): RatingViewModel {
    val application = LocalContext.current.applicationContext as CanteenCheckerApplication
    return viewModel {
        RatingViewModel(
            canteenId = canteenId,
            ratingApi = application.ratingApi,
            authentication = application.authentication,
        )
    }
}

@Composable
internal fun Rating(
    canteenId: String,
    onEditRating: () -> Unit,
    viewModel: RatingViewModel = ratingViewModel(canteenId)
) {
    Column(
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Your Rating",
            style = MaterialTheme.typography.titleMedium,
        )
        val ui = viewModel.ui
        when (ui) {
            RatingViewModel.UI.Loading -> CircularProgressIndicator()
            RatingViewModel.UI.Error -> ErrorUI(
                onRetry = viewModel::reload
            )
            RatingViewModel.UI.NotRated -> NotRated(
                onCreateRating = onEditRating,
                onSignOut = viewModel::signOut,
            )
            RatingViewModel.UI.NotSignedIn -> {
                val activity = LocalActivity.current
                NotSingedIn(
                    onSignedIn = { activity?.run(viewModel::signIn)}
                )
            }
            is RatingViewModel.UI.Rated -> Rated(
                ui.rating,
                onSignOut = viewModel::signOut,
                onEditRating = onEditRating,
                onDeleteRating = viewModel::deleteRating
            )
        }
    }
}

@Composable
private fun ErrorUI(
    onRetry: () -> Unit,
) {
    Text("Could not load rating.")
    Buttons {
        TextButton(
            onClick = onRetry
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun NotSingedIn(
    onSignedIn: () -> Unit,
) {
    Text("Not signed in.")
    Buttons {
        TextButton(
            onClick = onSignedIn
        ) {
            Text("SignIn")
        }
    }
}

@Composable
private fun NotRated(
    onCreateRating: () -> Unit,
    onSignOut: () -> Unit,
) {
    Text("You have not rated this canteen")
    Buttons {
        TextButton(
            onClick = onCreateRating
        ) {
            Text("Create rating")
        }
        TextButton(
            onClick = onSignOut
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun Rated(
    rating: Rating,
    onEditRating: () -> Unit,
    onDeleteRating: () -> Unit,
    onSignOut: () -> Unit,
) {
    Text("Your rating: ${rating.stars}") //TODO improve UI ...
    Buttons {
        TextButton(
            onClick = onEditRating
        ) {
            Text("Edit rating")
        }
        TextButton(
            onClick = onDeleteRating
        ) {
            Text("Delete rating")
        }
        TextButton(
            onClick = onSignOut
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
private fun Buttons(
    content: @Composable () -> Unit,
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

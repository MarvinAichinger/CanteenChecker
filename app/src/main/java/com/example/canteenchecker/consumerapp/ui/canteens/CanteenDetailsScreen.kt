package com.example.canteenchecker.consumerapp.ui.canteens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteenchecker.consumerapp.CanteenCheckerApplication
import com.example.canteenchecker.consumerapp.logic.CanteenApi
import com.example.canteenchecker.consumerapp.logic.CanteenDetails
import com.example.canteenchecker.consumerapp.ui.Error
import com.example.canteenchecker.consumerapp.ui.Loading
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

internal class CanteenDetailsViewModel(
    private val canteenId: String,
    private val canteenApi: CanteenApi,
) : ViewModel() {
    data class UI(
        var isLoading: Boolean = false,
        val data : Data = Data.Blank,
    ) {
        sealed interface Data {
            data object Blank : Data
            data object Error : Data
            data class Loaded(
                val canteen: CanteenDetails
            ) : Data
        }
    }

    var ui by mutableStateOf(UI())
        private set

    private var job: Job? = null

    init {
        load()
    }

    fun load() {
        ui = ui.copy(isLoading = false)
        job?.cancel()
        job = viewModelScope.launch {
            ui = ui.copy(isLoading = true)
            canteenApi.getCanteen(canteenId).onSuccess {
                ui = ui.copy(data = UI.Data.Loaded(it))
            }.onFailure {
                ui = ui.copy(data = UI.Data.Error)
            }
            ui = ui.copy(isLoading = false)
        }
    }
}


@Composable
private fun canteensViewModel(canteenId: String): CanteenDetailsViewModel {
    val application = LocalContext.current.applicationContext as CanteenCheckerApplication
    return viewModel {
        CanteenDetailsViewModel(canteenId, application.canteenApi)
    }
}

@Composable
internal fun CanteenDetailsScreen(
    canteenId: String,
    onClose: () -> Unit,
    onEditRating: () -> Unit,
    viewModel: CanteenDetailsViewModel = canteensViewModel(canteenId),
) {
    val ui = viewModel.ui
    Scaffold(
       topBar = {
           @OptIn(ExperimentalMaterial3Api::class)
           TopAppBar(
               title = { },
               navigationIcon = {
                   IconButton(
                       onClick = onClose,
                   ) {
                       Icon(
                           imageVector = Icons.AutoMirrored.Default.ArrowBack,
                           contentDescription = "Up"
                       )
                   }
               }
           )
       }
    ) { padding ->
        //TODO isLoading
        when (ui.data) {
            CanteenDetailsViewModel.UI.Data.Blank -> Loading(
                modifier = Modifier.padding(padding),
            )
            CanteenDetailsViewModel.UI.Data.Error -> Error(
                modifier = Modifier.padding(padding),
                message = "Failed to load canteen details",
                onRetry = viewModel::load,
            )
            is CanteenDetailsViewModel.UI.Data.Loaded -> Box(
                modifier = Modifier.padding(padding)
            ) {
                Content(
                    canteen = ui.data.canteen,
                    canteenId = canteenId,
                    onEditRating = onEditRating,
                )
            }
        }
    }
}

@Composable
internal fun Content(
    canteenId: String,
    canteen: CanteenDetails,
    onEditRating: () -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Details(
            canteen = canteen
        )
        Ratings(
            average = canteen.stars,
            count = canteen.ratingsCount,
        )
        Rating(
            canteenId = canteenId,
            onEditRating = onEditRating,
        )
    }
}

@Composable
private fun Details(
    canteen: CanteenDetails,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = canteen.name,
            style = MaterialTheme.typography.headlineLarge,
        )
        canteen.description?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    //TODO more data
    canteen.waitingTime?.let {
        //TODO
        Text(
            text = "$it",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun Ratings(
    average: Double?,
    count: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ratings",
            style = MaterialTheme.typography.titleMedium,
        )
        average?.let { stars ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "%.1f".format(stars),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row{
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint =
                                if (i < stars.roundToInt()) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } ?: Text(
            text = "No ratings yet",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}



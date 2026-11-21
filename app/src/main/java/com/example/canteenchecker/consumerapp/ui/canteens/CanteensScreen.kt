package com.example.canteenchecker.consumerapp.ui.canteens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteenchecker.consumerapp.CanteenCheckerApplication
import com.example.canteenchecker.consumerapp.logic.Canteen
import com.example.canteenchecker.consumerapp.ui.Error
import com.example.canteenchecker.consumerapp.ui.Loading
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class CanteensViewModel(application: Application): AndroidViewModel(application) {
    data class UI (
        val query: String = "",
        val isLoading: Boolean = false,
        val data: Data = Data.Blank,
    ) {
        sealed interface Data {
            data object Blank : Data
            data object Error : Data
            data class Loaded(
                val canteens: List<Canteen>,
                val more: Int?,
            ) : Data
        }
    }

    var ui by mutableStateOf(UI())
        private set

    private val canteenApi = (application as CanteenCheckerApplication).canteenApi
    private var job: Job? = null

    init {
        retry()
    }

    fun search(query: String) {
        ui = ui.copy(
            query = query,
            isLoading = false,
        )
        job?.cancel()
        job = viewModelScope.launch {
            ui = ui.copy( isLoading = true )
            canteenApi.getCanteens(query).onSuccess {
                ui = ui.copy(
                    data = UI.Data.Loaded(
                        canteens = it.canteens,
                        more = it.more,
                    )
                )
            }.onFailure {
                ui = ui.copy(
                    data = UI.Data.Error
                )
            }
            ui = ui.copy( isLoading = false )
        }
    }

    fun retry() {
        search(ui.query)
    }
}

@Composable
internal fun CanteensScreen(
    viewModel: CanteensViewModel = viewModel(),
    onCanteenSelected: (String) -> Unit,
) {
    val ui = viewModel.ui
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SearchField(
                value = ui.query,
                onChange = { query -> viewModel.search(query) }
            )
            when (ui.data) {
                CanteensViewModel.UI.Data.Blank -> Loading()
                CanteensViewModel.UI.Data.Error -> Error(
                    message = "Failed to load canteens",
                    onRetry = { viewModel.retry() }
                )
                is CanteensViewModel.UI.Data.Loaded -> PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = ui.isLoading,
                    onRefresh = { viewModel.retry() },
                ) {
                    CanteensList(
                        canteens = ui.data.canteens,
                        more = ui.data.more,
                        onCanteenClick = { onCanteenSelected(it.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onChange,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (value != "") {
                IconButton(
                    onClick = { onChange("") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                    )
                }
            }
        }
    )
}

@Composable
private fun CanteensList(
    canteens: List<Canteen>,
    more: Int?,
    onCanteenClick: (Canteen) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (canteens.isEmpty()) {
            item {
                Text(
                    text = "No matching canteens"
                )
            }
        } else {
            items(canteens, key = { it.id }) {
                CanteenCard(
                    canteen = it,
                    onClick = { onCanteenClick(it) },
                )
            }
            more?.let { count ->
                item {
                    Text(
                        text = "$count more"
                    )
                }
            }
        }
    }
}

@Composable
private fun CanteenCard(
    canteen: Canteen,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = canteen.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                canteen.menu?.let {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                    )
                }
                canteen.price?.let {
                    Text(
                        text = "EUR %.2f".format(it / 100f),
                    )
                }
            }
            canteen.stars?.let {
                Row(
                   horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "%.1f".format(it),
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

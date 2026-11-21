package com.example.canteenchecker.consumerapp.ui.ratings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.canteenchecker.consumerapp.CanteenCheckerApplication
import com.example.canteenchecker.consumerapp.ui.Error
import com.example.canteenchecker.consumerapp.ui.Loading

@Composable
private fun editRatingViewModel(canteenId: String): EditRatingViewModel {
    val application = LocalContext.current.applicationContext as CanteenCheckerApplication
    return viewModel { EditRatingViewModel(canteenId, application.ratingApi) }
}

@Composable
internal fun EditRatingScreen(
    canteenId: String,
    onClose: () -> Unit,
    viewModel: EditRatingViewModel = editRatingViewModel(canteenId),
) {
    val ui = viewModel.ui
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Your rating") },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                        )
                    }
                },
                actions = {
                    (ui as? EditRatingViewModel.UI.Input)?.let {
                        TextButton(
                            onClick = viewModel::save,
                        ) {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            when (ui) {
                EditRatingViewModel.UI.Loading -> Loading()
                EditRatingViewModel.UI.Error -> Error(
                    message = "Could not load your rating.",
                    onRetry = viewModel::load,
                )

                EditRatingViewModel.UI.Done -> {
                    val context = LocalContext.current
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "Rating saved", Toast.LENGTH_LONG).show()
                        onClose()
                    }
                }

                is EditRatingViewModel.UI.Input -> InputForm(
                    comment = ui.comment,
                    onCommentChange = viewModel::setComment,
                    stars = ui.stars,
                    onStarsChange = viewModel::setStars,
                    hasFailed = ui.hasFailed,
                )
            }
        }
    }

}

@Composable
private fun InputForm(
    comment: String,
    onCommentChange: (String) -> Unit,
    stars: Int,
    onStarsChange: (Int) -> Unit,
    hasFailed: Boolean,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row {
            for (value in 1..5) {
                IconButton(
                    onClick = { onStarsChange(value) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "$value stars",
                        tint = if (value <= stars) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Comment") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        if (hasFailed) {
            Text(
                text = "Saving your rating failed. Please try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
package com.example.canteenchecker.consumerapp.ui.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun HelloWorldScreen() {
    val count = remember { mutableStateOf(3) }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Hello World",
            )
            IncrementButton(
                onIncrement = { count.value++ }
            )
            Values(
                count = count.value,
            )
        }
    }
}

@Composable
private fun IncrementButton(
    onIncrement: () -> Unit,
) {
    Button(
        onClick = onIncrement,
    ) {
        Text("Increment")
    }
}

@Composable
private fun Values(
    count: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 1..count) {
            Text(
                text = "$i",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}
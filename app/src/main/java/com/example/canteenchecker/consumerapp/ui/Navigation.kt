package com.example.canteenchecker.consumerapp.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.canteenchecker.consumerapp.ui.canteens.CanteenDetailsScreen
import com.example.canteenchecker.consumerapp.ui.canteens.CanteensScreen
import com.example.canteenchecker.consumerapp.ui.ratings.EditRatingScreen
import kotlinx.serialization.Serializable

@Serializable
data object CanteensScreenKey : NavKey {
}

@Serializable
data class CanteenDetailsScreenKey(
    val canteenId: String,
) : NavKey {
}

@Serializable
data class EditRatingScreenKey(
    val canteenId: String,
): NavKey

@Composable
internal fun Navigation() {
    val backStack = rememberNavBackStack(CanteensScreenKey)
    NavDisplay(
        backStack = backStack,
        onBack = backStack::removeLastOrNull,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<CanteensScreenKey> {
                CanteensScreen(
                    onCanteenSelected = {
                        backStack.add(CanteenDetailsScreenKey(
                            canteenId = it
                        ))
                    },
                )
            }
            entry<CanteenDetailsScreenKey> {
                CanteenDetailsScreen(
                    canteenId = it.canteenId,
                    onClose = backStack::removeLastOrNull,
                    onEditRating = {
                        backStack.add(EditRatingScreenKey(
                            canteenId = it.canteenId
                        ))
                    }
                )
            }
            entry<EditRatingScreenKey> {
                EditRatingScreen(
                    canteenId = it.canteenId,
                    onClose = backStack::removeLastOrNull
                )
            }
        },
    )
}

package com.example.canteenchecker.consumerapp.logic

internal interface CanteenApi {
    suspend fun getCanteens(query: String): Result<CanteenResult>
    suspend fun getCanteen(canteenId: String): Result<CanteenDetails>
}

internal data class CanteenResult(
    val canteens: List<Canteen>,
    private val totalCount: Int,
) {
    val more = if (totalCount > canteens.size) totalCount - canteens.size else null
}

internal data class Canteen(
    val id: String,
    val name: String,
    val menu: String?,
    val price: Int?,
    val stars: Double?,
)

internal data class CanteenDetails(
    val name: String,
    val description: String?,
    val menu: String?,
    val price: Int?,
    val waitingTime: Int?,
    val stars: Double?,
    val ratingsCount: Int,
    //TODO...
)

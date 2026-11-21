package com.example.canteenchecker.consumerapp.logic

internal interface RatingApi {
    suspend fun getRating(canteenId: String): Result<Rating?>
    suspend fun resetRating(canteenId: String): Result<Unit>
    suspend fun setRating(canteenId: String, stars: Int, comment: String?): Result<Unit>
}

internal data class Rating(
    val stars: Int,
    val comment: String?,
)
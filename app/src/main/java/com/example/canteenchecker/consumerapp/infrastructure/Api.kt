package com.example.canteenchecker.consumerapp.infrastructure

import com.example.canteenchecker.consumerapp.logic.Canteen
import com.example.canteenchecker.consumerapp.logic.CanteenApi
import com.example.canteenchecker.consumerapp.logic.CanteenDetails
import com.example.canteenchecker.consumerapp.logic.CanteenResult
import com.example.canteenchecker.consumerapp.logic.Rating
import com.example.canteenchecker.consumerapp.logic.RatingApi
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okio.IOException
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

internal class Api(
    baseUrl: String,
    getAuthToken: suspend () -> String?,
) : CanteenApi, RatingApi {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        //AUTHENTICATION
        .client(
            OkHttpClient.Builder().addInterceptor { chain ->
                val token = runBlocking { getAuthToken() }
                val request =
                    if (token == null) chain.request()
                    else chain.request().newBuilder().header("Authorization", "Bearer $token").build()
                chain.proceed(request)
            }.build()
        )
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create<CanteenCheckerApi>()

    override suspend fun getCanteens(query: String): Result<CanteenResult> = api.call {
        //delay(1.seconds) //for testing
        getCanteens(query).let { result ->
            CanteenResult(
                canteens = result.items.map { canteen ->
                    Canteen(
                        id = canteen.id,
                        name = canteen.name,
                        menu = canteen.dailyMenu,
                        price = canteen.dailyMenuPrice,
                        stars = canteen.averageStars
                    )
                },
                totalCount = result.totalCount
            )
        }
    }

    override suspend fun getCanteen(canteenId: String): Result<CanteenDetails> = api.call {
        getCanteen(canteenId).let { canteen ->
            CanteenDetails(
                name = canteen.name,
                description = canteen.description,
                menu = canteen.dailyMenu,
                price = canteen.dailyMenuPrice,
                waitingTime = canteen.averageWaitingTime,
                stars = canteen.averageStars,
                ratingsCount = canteen.ratingsCount,
                //TODO continue here maybe...
            )
        }
    }

    override suspend fun getRating(canteenId: String): Result<Rating?> = api.call {
        getRating(canteenId).bodyOrNull { rating ->
            Rating(
                stars = rating.stars,
                comment = rating.comment,
            )
        }
    }

    private inline fun <T, R> Response<T>.bodyOrNull(block: (T) -> R): R? =
        if (!isSuccessful) throw HttpException(this) else body()?.let(block)

    override suspend fun resetRating(canteenId: String): Result<Unit> = api.call {
        deleteRating(canteenId)
    }

    override suspend fun setRating(
        canteenId: String,
        stars: Int,
        comment: String?
    ): Result<Unit> = api.call {
        putRating(
            canteenId = canteenId,
            body = RatingRequest(
                stars = stars,
                comment = comment,
            )
        )
    }
}

private interface CanteenCheckerApi {
    @GET("api/v1/consumer/canteens")
    suspend fun getCanteens(@Query("query") query: String) : CanteenListResponse

    @GET("api/v1/consumer/canteens/{canteenId}")
    suspend fun getCanteen(@Path("canteenId") canteenId: String): CanteenDetailsResponse

    @GET("api/v1/consumer/canteens/{canteenId}/rating")
    suspend fun getRating(@Path("canteenId") canteenId: String):
            //!!! Retrofit does not support nullable return types out of the boy yet :(
            Response<RatingsResponse>

    @PUT("api/v1/consumer/canteens/{canteenId}/rating")
    suspend fun putRating(
        @Path("canteenId") canteenId: String,
        @Body body: RatingRequest
    )

    @DELETE("api/v1/consumer/canteens/{canteenId}/rating")
    suspend fun deleteRating(@Path("canteenId") canteenId: String)
}

@Serializable
private data class CanteenDetailsResponse(
    val averageStars: Double?,
    val averageWaitingTime: Int?,
    val dailyMenu: String?,
    val dailyMenuPrice: Int?,
    val description: String?,
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val ratingsCount: Int,
    val websiteUrl: String?,
)

@Serializable
private data class CanteenListResponse(
    val items: List<CanteenListItemResponse>,
    val totalCount: Int,
)

@Serializable
private data class CanteenListItemResponse(
    val averageStars: Double?,
    val dailyMenu: String?,
    val dailyMenuPrice: Int?,
    val id: String,
    val name: String,
)

@Serializable
private data class RatingRequest(
    val stars: Int,
    val comment: String?,
)

@Serializable
private data class RatingsResponse(
    val stars: Int,
    val comment: String?,
    //TODO createdAt, updatedAt - we do not use them currently...
)


private inline fun <T> CanteenCheckerApi.call(block: CanteenCheckerApi.() -> T): Result<T> = try {
    Result.success(block())
} catch (e: IOException) {
    Result.failure(e)
} catch (e: HttpException) {
    Result.failure(e)
}


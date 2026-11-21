package com.example.canteenchecker.consumerapp

import android.app.Application
import com.example.canteenchecker.consumerapp.infrastructure.Api
import com.example.canteenchecker.consumerapp.infrastructure.MsalAuthentication
import com.example.canteenchecker.consumerapp.logic.Authentication
import com.example.canteenchecker.consumerapp.logic.CanteenApi
import com.example.canteenchecker.consumerapp.logic.RatingApi
import kotlin.getValue

class CanteenCheckerApplication : Application() {
    private val api by lazy {
        Api(
            baseUrl = "https://moc5.projekte.fh-hagenberg.at/CanteenChecker/",
            getAuthToken = auth::getAccessToken,
        )
    }

    private val auth by lazy {
        MsalAuthentication(this)
    }

    internal val canteenApi: CanteenApi
        get() = api

    internal val ratingApi: RatingApi
        get() = api

    internal val authentication: Authentication
        get() = auth
}

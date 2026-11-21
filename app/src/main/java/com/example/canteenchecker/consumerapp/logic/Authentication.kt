package com.example.canteenchecker.consumerapp.logic

import android.app.Activity

internal interface Authentication {
    suspend fun authenticated(): Boolean
    suspend fun signIn(activity: Activity)
    suspend fun signOut()
}


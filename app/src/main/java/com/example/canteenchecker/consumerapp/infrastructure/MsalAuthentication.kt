package com.example.canteenchecker.consumerapp.infrastructure

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.canteenchecker.consumerapp.R
import com.example.canteenchecker.consumerapp.logic.Authentication
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

internal class MsalAuthentication(context: Context) : Authentication {
    private val scopes = listOf("api://875ce4a5-cfee-4699-89f7-51451c22c962/api_access")

    private val app by lazy {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.msal_config
        )
    }

    suspend fun getAccessToken() = withContext(Dispatchers.IO) {
        app.currentAccount.currentAccount?.let { account ->
            try {
                app.acquireTokenSilent(
                    AcquireTokenSilentParameters.Builder()
                        .forAccount(account)
                        .fromAuthority(account.authority)
                        .withScopes(scopes)
                        .build()
                ).accessToken
            } catch (e: MsalException) {
                Log.e(MsalAuthentication::class.simpleName, "Failed to acquire token", e)
                null
            }
        }
    }

    override suspend fun signOut() = withContext(Dispatchers.IO) {
        if (authenticated()) app.signOut()
        Unit
    }

    override suspend fun signIn(activity: Activity) {
        val app = withContext(Dispatchers.IO) { app }
        suspendCancellableCoroutine { continuation ->
            app.signIn(
                SignInParameters.builder()
                    .withScopes(scopes)
                    .withActivity(activity)
                    .withCallback(object : AuthenticationCallback {
                        override fun onSuccess(result: IAuthenticationResult) {
                            continuation.resume(Unit)
                        }

                        override fun onError(exception: MsalException) {
                            Log.e(MsalAuthentication::class.simpleName, "Sign-in failed", exception)
                            continuation.resume(Unit)
                        }

                        override fun onCancel() {
                            continuation.resume(Unit)
                        }
                    })
                    .build()
            )
        }
    }

    override suspend fun authenticated() = withContext(Dispatchers.IO) {
        app.currentAccount.currentAccount != null
    }
}
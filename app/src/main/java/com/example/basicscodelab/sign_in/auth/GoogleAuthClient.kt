package com.example.basicscodelab.sign_in.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import com.example.basicscodelab.R
import com.example.basicscodelab.data.ListViewModel
import com.example.basicscodelab.sign_in.SignInResult
import com.example.basicscodelab.sign_in.UserData
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient,
    private val listViewModel: ListViewModel) : Authentication {
    private val auth = Firebase.auth

    @Override
    override suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: ApiException) {
            Toast.makeText(
                context,
                "No matching credentials",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
//            if(e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    @Override
    override suspend fun signInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            if (user != null) {
                Log.d("Login Success", "User ID: ${user.uid}, Timestamp: ${System.currentTimeMillis()}")
                // Attempting to add the user to the database upon successful login:
                listViewModel.handleUserLogin(UserData(
                    userId = user.uid,
                    userName = user.displayName,
                    profilePictureUrl = user.photoUrl?.toString()
                ))
            } else {
                Log.e("Login Failure", "User is null")
                // handle failure or return an appropriate result
            }
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        userName = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            Log.e("Login failure", "Exception: ${e.message}")
            if(e is CancellationException) throw e
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    @Override
    override suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
            if(e is CancellationException) throw e
        }
    }

    @Override
    override fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        // Add the web_id to the strings.xml instead of hardcoding it
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}
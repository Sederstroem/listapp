package com.example.basicscodelab.sign_in.auth

import android.content.Intent
import android.content.IntentSender
import com.example.basicscodelab.sign_in.SignInResult
import com.example.basicscodelab.sign_in.UserData

interface Authentication {
    suspend fun signIn(): IntentSender?
    fun getSignedInUser(): UserData?
    suspend fun signInWithIntent(intent: Intent): SignInResult
    suspend fun signOut()
}
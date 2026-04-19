package com.rajsv.exitsense.data.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.rajsv.exitsense.R
import com.rajsv.exitsense.data.model.UserDataStore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                UserDataStore.login(
                    context,
                    name = user.displayName ?: "User",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString()
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to get user from Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        getGoogleSignInClient().signOut().await()
        UserDataStore.logout(context)
    }
}
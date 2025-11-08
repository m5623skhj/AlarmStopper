package com.alarmstopper.data.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser, val isAnonymous: Boolean) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

interface AuthRepository {
    suspend fun signInAnonymously(): Result<FirebaseUser>
    suspend fun linkWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser>
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser>
    fun getCurrentUser(): FirebaseUser?
    fun isAnonymous(): Boolean
    suspend fun signOut()
}

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    private val TAG = "FirebaseAuthRepository"

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            Timber.d("Attempting anonymous sign in...")
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw IllegalStateException("User is null")
            Timber.d("Anonymous sign in successful: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Anonymous sign in failed")
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("User is null")
            Timber.d("Google sign in successful: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Timber.e(e, "Google sign in failed")
            Result.failure(e)
        }
    }

    override suspend fun linkWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val currentUser = auth.currentUser
                ?: throw IllegalStateException("No current user")

            if (!currentUser.isAnonymous) {
                throw IllegalStateException("Current user is not anonymous")
            }

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = currentUser.linkWithCredential(credential).await()
            val user = result.user ?: throw IllegalStateException("User is null")
            Timber.d("Successfully linked with Google: ${user.uid}")
            Result.success(user)
        } catch (e: FirebaseAuthUserCollisionException) {
            Timber.e(e, "Google account already exists")
            Result.failure(Exception("이 Google 계정은 이미 사용 중입니다"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to link with Google")
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous ?: false

    override suspend fun signOut() {
        auth.signOut()
        Timber.d("User signed out")
    }
}
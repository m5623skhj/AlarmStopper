package com.alarmstopper.domain.usecase

import com.alarmstopper.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser

class UpgradeToGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(account: GoogleSignInAccount): Result<FirebaseUser> {
        return if (authRepository.isAnonymous()) {
            authRepository.linkWithGoogle(account)
        } else {
            authRepository.signInWithGoogle(account)
        }
    }
}
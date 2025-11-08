package com.alarmstopper.domain.usecase

import com.alarmstopper.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser

class InitializeAuthUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<FirebaseUser> {
        authRepository.getCurrentUser()?.let {
            return Result.success(it)
        }

        return authRepository.signInAnonymously()
    }
}
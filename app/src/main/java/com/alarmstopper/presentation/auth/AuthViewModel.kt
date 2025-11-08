package com.alarmstopper.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmstopper.data.repository.AuthState
import com.alarmstopper.domain.usecase.InitializeAuthUseCase
import com.alarmstopper.domain.usecase.UpgradeToGoogleUseCase
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val initializeAuthUseCase: InitializeAuthUseCase,
    private val upgradeToGoogleUseCase: UpgradeToGoogleUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        initializeAuth()
    }

    private fun initializeAuth() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            initializeAuthUseCase()
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(
                        user = user,
                        isAnonymous = user.isAnonymous
                    )
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "로그인 실패")
                }
        }
    }

    fun upgradeToGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            upgradeToGoogleUseCase(account)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(
                        user = user,
                        isAnonymous = false
                    )
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Google 연동 실패")
                }
        }
    }
}
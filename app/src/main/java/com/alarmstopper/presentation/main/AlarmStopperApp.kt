package com.alarmstopper.presentation.main

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alarmstopper.data.repository.AuthState
import com.alarmstopper.presentation.auth.AuthViewModel
import com.alarmstopper.presentation.alarmlist.AlarmListScreen
import com.alarmstopper.presentation.common.*

@Composable
fun AlarmStopperApp(
    authViewModel: AuthViewModel,
    onUpgradeClick: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            LoadingScreen()
        }

        is AuthState.Authenticated -> {
            MainNavigation(
                isAnonymous = state.isAnonymous,
                onUpgradeClick = onUpgradeClick
            )
        }

        is AuthState.Unauthenticated -> {
            ErrorScreen(message = "인증이 필요합니다")
        }

        is AuthState.Error -> {
            ErrorScreen(message = state.message)
        }
    }
}

@Composable
fun MainNavigation(
    isAnonymous: Boolean,
    onUpgradeClick: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "alarm_list"
    ) {
        composable("alarm_list") {
            AlarmListScreen(
                isAnonymous = isAnonymous,
                onUpgradeClick = onUpgradeClick,
                onNavigateToSettings = { }
            )
        }
    }
}
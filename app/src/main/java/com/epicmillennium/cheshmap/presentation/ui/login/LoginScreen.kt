package com.epicmillennium.cheshmap.presentation.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions

@Composable
fun LoginScreen(
    waterSourceId: String,
    isPickingLocationForNewWaterSource: Boolean,
    navigationActions: AppNavigationActions
) {

    val loginViewModel: LoginViewModel = hiltViewModel()

    val loginUiState by loginViewModel.loginUiState.collectAsStateWithLifecycle()

    LoginView(
        waterSourceId,
        isPickingLocationForNewWaterSource,
        navigationActions,
        loginUiState,
        signInWithEmail = loginViewModel::signInWithEmail,
        loginWithGoogle = loginViewModel::signInWithGoogle,
        signUpWithEmail = loginViewModel::signUpWithEmail,
    )
}

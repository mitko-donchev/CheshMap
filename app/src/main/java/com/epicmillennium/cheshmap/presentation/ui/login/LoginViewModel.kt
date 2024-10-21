package com.epicmillennium.cheshmap.presentation.ui.login

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.credentials.Credential
import androidx.credentials.CustomCredential
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epicmillennium.cheshmap.domain.usecase.auth.SignInWithEmailUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignInWithGoogleUseCase
import com.epicmillennium.cheshmap.domain.usecase.auth.SignUpWithEmailUseCase
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase
) : ViewModel() {

    private val _loginUiState = MutableStateFlow(LoginViewContentState.Success(false))
    val loginUiState = _loginUiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(3000L),
        LoginViewContentState.Success(false)
    )

    fun signInWithEmail(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        signInWithEmailUseCase.invoke(email, password).fold({
            Log.d("LoginViewModel", "Sign in with Email success")
            _loginUiState.update {
                LoginViewContentState.Success(true)
            }
        }, {
            Log.e("LoginViewModel", "Sign in with Email failed: ${it.message}")
        })
    }

    fun signInWithGoogle(googleCredentials: Credential) = viewModelScope.launch(Dispatchers.IO) {
        if (googleCredentials is CustomCredential && googleCredentials.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(googleCredentials.data)
            signInWithGoogleUseCase.invoke(googleIdTokenCredential.idToken).fold({
                Log.d("LoginViewModel", "Sign in with Google success")
                _loginUiState.update {
                    LoginViewContentState.Success(true)
                }
            }, {
                Log.e("LoginViewModel", "Sign in with Google failed: ${it.message}")
            })
        } else {
            Log.e("LoginViewModel", "Invalid credential type: ${googleCredentials.type}")
        }
    }

    fun signUpWithEmail(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        signUpWithEmailUseCase.invoke(email, password).fold({
            Log.d("LoginViewModel", "Sign up with Email success")
            _loginUiState.update {
                LoginViewContentState.Success(true)
            }
        }, {
            Log.e("LoginViewModel", "Sign up with Email failed: ${it.message}")
        })
    }
}

@Immutable
sealed class LoginViewContentState {
    data object Loading : LoginViewContentState()
    data class Success(val isUserLoggedIn: Boolean) : LoginViewContentState()
    data class Error(val message: String) : LoginViewContentState()
}
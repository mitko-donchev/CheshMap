package com.epicmillennium.cheshmap.presentation.ui.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.compose.rememberNavController
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.theme.CheshMapTheme
import com.epicmillennium.cheshmap.presentation.theme.DarkTheme
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import com.epicmillennium.cheshmap.presentation.ui.navigation.AppNavigationActions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginView(
    waterSourceId: String,
    isPickingLocationForNewWaterSource: Boolean,
    navigationActions: AppNavigationActions,
    loginUiState: LoginViewContentState,
    signInWithEmail: (String, String) -> Job,
    loginWithGoogle: (Credential) -> Job,
    signUpWithEmail: (String, String) -> Job
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back press
    BackHandler { navigationActions.navigateToLending(waterSourceId, isPickingLocationForNewWaterSource) }

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            when (loginUiState) {
                is LoginViewContentState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(48.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is LoginViewContentState.Success -> {
                    var email by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }

                    LaunchedEffect(loginUiState.isUserLoggedIn) {
                        if (loginUiState.isUserLoggedIn) {
                            navigationActions.navigateToLending(waterSourceId, isPickingLocationForNewWaterSource)
                        }
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { },
                                navigationIcon = {
                                    IconButton(onClick = { navigationActions.navigateToLending() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.navigate_back)
                                        )
                                    }
                                },
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_account),
                                    contentDescription = "Login screen icon",
                                    modifier = Modifier.size(64.dp),
                                    contentScale = ContentScale.Fit,
                                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Sign in",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Box(
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        OutlinedTextField(
                                            value = email,
                                            onValueChange = { email = it.trim() },
                                            label = { Text("Email") },
                                            keyboardOptions = KeyboardOptions(
                                                capitalization = KeyboardCapitalization.None,
                                                autoCorrectEnabled = false,
                                                keyboardType = KeyboardType.Email
                                            ),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Email,
                                                    contentDescription = "Email icon"
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it.trim() },
                                            label = { Text("Password") },
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Password
                                            ),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = "Password icon"
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            visualTransformation = PasswordVisualTransformation()
                                        )

                                        Box(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            TextButton(
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .padding(end = 8.dp),
                                                onClick = { /* Handle Forgot Password */ }) {
                                                Text(
                                                    text = "Forgot Password",
                                                    color = Color.Gray,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (verifyItsAValidEmail(email)) {
                                                    signInWithEmail.invoke(email, password)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                        ) {
                                            Text(text = "Sign in", color = Color.White)
                                        }

                                        TextButton(onClick = { /* Handle Create Account */ }) {
                                            Text(
                                                text = "Create an account",
                                                color = Color.Gray,
                                                fontSize = 14.sp
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    start = 16.dp,
                                                    bottom = 12.dp,
                                                    end = 16.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 16.dp)
                                            )
                                            Text(text = "Or", color = Color.Gray, fontSize = 14.sp)
                                            HorizontalDivider(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 16.dp)
                                            )
                                        }

                                        // Sign in with Google button
                                        Button(
                                            onClick = {
                                                signInWithGoogle(
                                                    context,
                                                    coroutineScope,
                                                    loginWithGoogle
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.ic_google), // Replace with your image resource
                                                    contentDescription = "Google Logo",
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "Continue with Google",
                                                    color = Color.White,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is LoginViewContentState.Error -> {
                    LaunchedEffect(loginUiState.message) {
                        snackbarHostState.showSnackbar(
                            message = loginUiState.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }
}

fun verifyItsAValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun signInWithGoogle(
    context: Context,
    coroutineScope: CoroutineScope,
    loginWithGoogle: (Credential) -> Job
) {
    val credentialManager = CredentialManager.create(context)

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(context.getString(R.string.default_web_client_id))
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    coroutineScope.launch {
        try {
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            loginWithGoogle.invoke(result.credential)
        } catch (e: GetCredentialException) {
            Log.e("LoginView", "Error signing in with Google", e)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginViewPreview() {
    LoginView(
        waterSourceId = "",
        isPickingLocationForNewWaterSource = false,
        navigationActions = AppNavigationActions(rememberNavController()),
        loginUiState = LoginViewContentState.Success(false),
        signInWithEmail = { _, _ -> Job() },
        loginWithGoogle = { Job() },
        signUpWithEmail = { _, _ -> Job() }
    )
}
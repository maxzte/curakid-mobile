package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.curakid.core.common.util.ValidationUtils

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    emailError: String? = null,
    passwordError: String? = null
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isEmailValid = remember(email) { ValidationUtils.isValidEmail(email) }
    val isFormValid = isEmailValid && password.isNotBlank()

    // Focus chain: email → password → submit
    val passwordFocus = remember { FocusRequester() }

    fun submit() {
        viewModel.login(email, password)
        if (uiState is UiState.Success) {
            onNavigateToSignUp()
        }
    }

    AuthScaffold {
        // Logo + title
        AuthHeader(
            title = "Вітаємо!",
            subtitle = "Увійдіть до свого акаунту"
        )

        Spacer(Modifier.height(32.dp))

        // Email
        CuraKidTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocus.requestFocus() }
            ),
            error = emailError
        )

        Spacer(Modifier.height(12.dp))

        // Password
        CuraKidTextField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submit() }
            ),
            error = passwordError,
            modifier = Modifier.focusRequester(passwordFocus)
        )

        Spacer(Modifier.height(8.dp))

        AuthSwitchText(
            text = "",
            actionText = "Забули пароль?",
            onClick = onNavigateToForgotPassword,
        )

        Spacer(Modifier.height(24.dp))

        // Login button
        AuthPrimaryButton(
            text = "Увійти",
            isLoading = uiState is UiState.Loading,
            enabled = isFormValid,
            onClick = ::submit
        )

        Spacer(Modifier.height(16.dp))

        AuthDivider()

        Spacer(Modifier.height(16.dp))

        // Google sign-in
        GoogleButton(onTokenReceived = viewModel::oAuthLogin)

        Spacer(Modifier.height(32.dp))

        // Navigate to register
        AuthSwitchText(
            text = "Ще немає акаунту? ",
            actionText = "Зареєструватись",
            onClick = onNavigateToSignUp,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginWithErrorsPreview() {
    MaterialTheme {
        LoginScreen(
            emailError = "Невірний формат email",
            passwordError = "Пароль має бути не менше 8 символів"
        )
    }
}

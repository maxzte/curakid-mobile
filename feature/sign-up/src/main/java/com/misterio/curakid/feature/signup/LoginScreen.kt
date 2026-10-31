package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.curakid.core.common.util.ValidationUtils

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    emailError: String? = null,
    passwordError: String? = null,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LoginContent(
        uiState = uiState,
        onLogin = { email, password -> viewModel.login(email, password) },
        onOAuthLogin = { token -> viewModel.oAuthLogin(token) },
        onNavigateToSignUp = onNavigateToSignUp,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        emailError = emailError,
        passwordError = passwordError
    )
}

@Composable
fun LoginContent(
    uiState: UiState<String>,
    onLogin: (String, String) -> Unit,
    onOAuthLogin: (String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    emailError: String? = null,
    passwordError: String? = null,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isEmailValid = remember(email) { ValidationUtils.isValidEmail(email) }
    val isFormValid = isEmailValid && password.isNotBlank()

    val passwordFocus = remember { FocusRequester() }

    fun submit() {
        if (isFormValid) {
            onLogin(email, password)
        }
    }

    AuthScaffold(modifier = modifier) {
        AuthHeader(
            title = stringResource(R.string.login_welcome),
            subtitle = stringResource(R.string.login_subtitle)
        )

        Spacer(Modifier.height(32.dp))

        CuraKidTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.email_label),
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

        CuraKidTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.password_label),
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
            actionText = stringResource(R.string.login_forgot_password),
            onClick = onNavigateToForgotPassword,
        )

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.login_button),
            isLoading = uiState is UiState.Loading,
            enabled = isFormValid,
            onClick = ::submit
        )

        Spacer(Modifier.height(16.dp))

        AuthDivider()

        Spacer(Modifier.height(16.dp))

        GoogleButton(onTokenReceived = onOAuthLogin)

        Spacer(Modifier.height(32.dp))

        AuthSwitchText(
            text = stringResource(R.string.login_no_account),
            actionText = stringResource(R.string.login_register_action),
            onClick = onNavigateToSignUp,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginContent(
        uiState = UiState.Initial,
        onLogin = { _, _ -> },
        onOAuthLogin = {},
        onNavigateToSignUp = {},
        onNavigateToForgotPassword = {}
    )
}

@Preview(showBackground = true)
@Composable
fun LoginWithErrorsPreview() {
    LoginContent(
        uiState = UiState.Initial,
        onLogin = { _, _ -> },
        onOAuthLogin = {},
        onNavigateToSignUp = {},
        onNavigateToForgotPassword = {},
        emailError = "Невірний формат email",
        passwordError = "Пароль має бути не менше 8 символів"
    )
}

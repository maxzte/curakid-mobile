package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun SignUpScreen(
    onNavigateToLogIn: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    nameError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val error = (uiState as? UiState.Error)?.errorMessage
    val currentEmailError = emailError ?: if (error == "Невірний формат email") error else null

    // Focus chain: name → email → password → submit
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onNavigateToVerify(email)
        }
    }

    fun submit() {
        viewModel.signUp(email, password, name)
    }

    AuthScaffold {
        AuthHeader(
            title = "Реєстрація",
            subtitle = "Створіть акаунт"
        )

        Spacer(Modifier.height(32.dp))

        // Name
        CuraKidTextField(
            value = name,
            onValueChange = { name = it },
            label = "Ім'я та прізвище",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { emailFocus.requestFocus() }
            ),
            error = nameError
        )

        Spacer(Modifier.height(12.dp))

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
            error = currentEmailError,
            modifier = Modifier.focusRequester(emailFocus)
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

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = "Зареєструватись",
            isLoading = uiState is UiState.Loading,
            onClick = ::submit
        )

        Spacer(Modifier.height(16.dp))

        AuthDivider()

        Spacer(Modifier.height(16.dp))

        GoogleButton(onClick = {})

        Spacer(Modifier.height(32.dp))

        AuthSwitchText(
            text = "Вже є акаунт? ",
            actionText = "Увійти",
            onClick = onNavigateToLogIn
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        SignUpScreen({}, {})
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterWithErrorPreview() {
    MaterialTheme {
        SignUpScreen(
            onNavigateToLogIn = {},
            onNavigateToVerify = {},
            emailError = "Невірний формат email"
        )
    }
}

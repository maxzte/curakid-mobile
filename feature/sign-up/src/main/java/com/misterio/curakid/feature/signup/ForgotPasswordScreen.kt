package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.CuraKidTextField

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val step by viewModel.step.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    AuthScaffold {
        AuthHeader(
            title = "Відновлення",
            subtitle = when (step) {
                ForgotPasswordStep.RequestEmail -> "Введіть ваш email для отримання інструкцій"
                ForgotPasswordStep.ResetPassword -> "Введіть код з пошти та новий пароль"
                ForgotPasswordStep.Success -> "Пароль успішно змінено!"
            }
        )

        Spacer(Modifier.height(32.dp))

        when (step) {
            ForgotPasswordStep.RequestEmail -> {
                CuraKidTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.requestReset(email) }
                    ),
                    error = (uiState as? UiState.Error)?.errorMessage
                )

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = "Надіслати",
                    isLoading = uiState is UiState.Loading,
                    onClick = { viewModel.requestReset(email) }
                )
            }

            ForgotPasswordStep.ResetPassword -> {
                CuraKidTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Код підтвердження",
                    leadingIcon = Icons.Default.Numbers,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    error = (uiState as? UiState.Error)?.errorMessage
                )

                Spacer(Modifier.height(16.dp))

                CuraKidTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Новий пароль",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { viewModel.resetPassword(email, code, newPassword) }
                    )
                )

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = "Змінити пароль",
                    isLoading = uiState is UiState.Loading,
                    onClick = { viewModel.resetPassword(email, code, newPassword) }
                )
            }

            ForgotPasswordStep.Success -> {
                AuthPrimaryButton(
                    text = "До логіну",
                    isLoading = false,
                    onClick = onBack
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AuthSwitchText(
            text = "Згадали пароль? ",
            actionText = "Увійти",
            onClick = onBack
        )
    }
}

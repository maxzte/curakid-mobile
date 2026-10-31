package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val step by viewModel.step.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        step = step,
        uiState = uiState,
        onRequestReset = viewModel::requestReset,
        onResetPassword = viewModel::resetPassword,
        onBack = onBack
    )
}

@Composable
fun ForgotPasswordContent(
    step: ForgotPasswordStep,
    uiState: UiState<Unit>,
    onRequestReset: (email: String) -> Unit,
    onResetPassword: (email: String, code: String, newPass: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    AuthScaffold(modifier = modifier) {
        AuthHeader(
            title = stringResource(R.string.forgot_password_title),
            subtitle = when (step) {
                ForgotPasswordStep.RequestEmail -> stringResource(R.string.forgot_password_step_email)
                ForgotPasswordStep.ResetPassword -> stringResource(R.string.forgot_password_step_reset)
                ForgotPasswordStep.Success -> stringResource(R.string.forgot_password_step_success)
            }
        )

        Spacer(Modifier.height(32.dp))

        when (step) {
            ForgotPasswordStep.RequestEmail -> {
                CuraKidTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(R.string.email_label),
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onRequestReset(email) }
                    ),
                    error = (uiState as? UiState.Error)?.errorMessage
                )

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = stringResource(R.string.forgot_password_send_button),
                    isLoading = uiState is UiState.Loading,
                    onClick = { onRequestReset(email) }
                )
            }

            ForgotPasswordStep.ResetPassword -> {
                CuraKidTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = stringResource(R.string.forgot_password_code_label),
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
                    label = stringResource(R.string.forgot_password_new_password_label),
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onResetPassword(email, code, newPassword) }
                    )
                )

                Spacer(Modifier.height(24.dp))

                AuthPrimaryButton(
                    text = stringResource(R.string.forgot_password_change_button),
                    isLoading = uiState is UiState.Loading,
                    onClick = { onResetPassword(email, code, newPassword) }
                )
            }

            ForgotPasswordStep.Success -> {
                AuthPrimaryButton(
                    text = stringResource(R.string.forgot_password_to_login_button),
                    isLoading = false,
                    onClick = onBack
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AuthSwitchText(
            text = stringResource(R.string.forgot_password_remembered_prefix),
            actionText = stringResource(R.string.forgot_password_login_action),
            onClick = onBack
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordRequestEmailPreview() {
    CurakidTheme {
        ForgotPasswordContent(
            step = ForgotPasswordStep.RequestEmail,
            uiState = UiState.Initial,
            onRequestReset = {},
            onResetPassword = { _, _, _ -> },
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ForgotPasswordResetPreview() {
    CurakidTheme {
        ForgotPasswordContent(
            step = ForgotPasswordStep.ResetPassword,
            uiState = UiState.Initial,
            onRequestReset = {},
            onResetPassword = { _, _, _ -> },
            onBack = {}
        )
    }
}

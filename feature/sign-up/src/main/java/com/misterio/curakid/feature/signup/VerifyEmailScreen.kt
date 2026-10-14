package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.CuraKidTextField

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerificationSuccess: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            onVerificationSuccess()
        }
    }

    AuthScaffold {
        AuthHeader(
            title = stringResource(R.string.verify_email_title),
            subtitle = stringResource(R.string.verify_email_subtitle, email)
        )

        Spacer(Modifier.height(32.dp))

        CuraKidTextField(
            value = code,
            onValueChange = { code = it },
            label = stringResource(R.string.verify_email_button),
            leadingIcon = Icons.Default.Numbers,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { viewModel.verifyEmail(email, code) }
            ),
            error = if (uiState is UiState.Error) (uiState as UiState.Error).errorMessage else null
        )

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.verify_email_button),
            isLoading = uiState is UiState.Loading,
            onClick = { viewModel.verifyEmail(email, code) }
        )
    }
}

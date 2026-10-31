package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

@Composable
fun VerifyEmailScreen(
    email: String,
    onVerificationSuccess: () -> Unit,
    viewModel: VerifyEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            viewModel.resetState()
            onVerificationSuccess()
        }
    }

    VerifyEmailContent(
        email = email,
        uiState = uiState,
        onVerify = { code -> viewModel.verifyEmail(email, code) }
    )
}

@Composable
fun VerifyEmailContent(
    email: String,
    uiState: UiState<Unit>,
    onVerify: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by rememberSaveable { mutableStateOf("") }

    AuthScaffold(modifier = modifier) {
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
                onDone = { onVerify(code) }
            ),
            error = if (uiState is UiState.Error) uiState.errorMessage else null
        )

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.verify_email_button),
            isLoading = uiState is UiState.Loading,
            onClick = { onVerify(code) }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyEmailScreenPreview() {
    VerifyEmailContent(
        email = "test@example.com",
        uiState = UiState.Initial,
        onVerify = {}
    )
}

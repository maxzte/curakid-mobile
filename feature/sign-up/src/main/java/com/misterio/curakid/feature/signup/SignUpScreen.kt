package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.ConsentType
import com.misterio.core.domain.model.LegalDocument
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.curakid.core.common.util.ValidationUtils

@Composable
fun SignUpScreen(
    onNavigateToLogIn: () -> Unit,
    onNavigateToVerify: (String) -> Unit,
    onShowDocument: (String, String) -> Unit = { _, _ -> },
    nameError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val legalDocs by viewModel.legalDocuments.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is UiState.Success) {
            val data = state.data
            viewModel.resetState()
            if (data != "Login successful") {
                onNavigateToVerify(data)
            }
        }
    }

    SignUpContent(
        uiState = uiState,
        legalDocs = legalDocs,
        onSignUp = { email, password, name, isAccepted ->
            viewModel.signUp(email, password, name, isAccepted)
        },
        onOAuthLogin = { token -> viewModel.oAuthLogin(token) },
        onNavigateToLogIn = onNavigateToLogIn,
        onShowDocument = onShowDocument,
        nameError = nameError,
        emailError = emailError,
        passwordError = passwordError
    )
}

@Composable
fun SignUpContent(
    uiState: UiState<String>,
    legalDocs: UiState<List<LegalDocument>>,
    onSignUp: (String, String, String, Boolean) -> Unit,
    onOAuthLogin: (String) -> Unit,
    onNavigateToLogIn: () -> Unit,
    onShowDocument: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    nameError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isAccepted by rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) { ValidationUtils.isValidEmail(email) }
    val isPasswordValid = remember(password) { ValidationUtils.isValidPassword(password) }
    val isFormValid = name.isNotBlank() && isEmailValid && isPasswordValid && isAccepted

    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }

    val currentEmailError = emailError ?: if (uiState is UiState.Error) uiState.errorMessage else null

    fun submit() {
        if (isFormValid) {
            onSignUp(email, password, name, isAccepted)
        }
    }

    val termsTitle = stringResource(R.string.signup_terms_title)
    val privacyTitle = stringResource(R.string.signup_privacy_title)

    val termsUrl = remember(legalDocs) {
        (legalDocs as? UiState.Success)?.data?.find { it.type == ConsentType.TERMS }?.url
    }
    val privacyUrl = remember(legalDocs) {
        (legalDocs as? UiState.Success)?.data?.find { it.type == ConsentType.PRIVACY_POLICY }?.url
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val annotatedString = buildAnnotatedString {
        append(stringResource(R.string.signup_agree_prefix))

        val linkStyle = TextLinkStyles(
            style = SpanStyle(
                color = primaryColor,
                fontWeight = FontWeight.Bold
            )
        )

        withLink(
            LinkAnnotation.Clickable(
                tag = "TERMS",
                styles = linkStyle,
                linkInteractionListener = {
                    termsUrl?.let { onShowDocument(it, termsTitle) }
                }
            )
        ) {
            withStyle(
                SpanStyle(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(stringResource(R.string.signup_terms_action))
            }
        }

        append(stringResource(R.string.signup_and_conjunction))

        withLink(
            LinkAnnotation.Clickable(
                tag = "PRIVACY",
                styles = linkStyle,
                linkInteractionListener = {
                    privacyUrl?.let { onShowDocument(it, privacyTitle) }
                }
            )
        ) {
            withStyle(
                SpanStyle(
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(stringResource(R.string.signup_privacy_action))
            }
        }
    }

    AuthScaffold(modifier = modifier) {
        AuthHeader(
            title = stringResource(R.string.signup_title),
            subtitle = stringResource(R.string.signup_subtitle)
        )

        Spacer(Modifier.height(32.dp))

        CuraKidTextField(
            value = name,
            onValueChange = { name = it },
            label = stringResource(R.string.signup_name_label),
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
            error = currentEmailError,
            modifier = Modifier.focusRequester(emailFocus)
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

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = { isAccepted = it },
            )
            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        AuthPrimaryButton(
            text = stringResource(R.string.signup_button),
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
            text = stringResource(R.string.signup_has_account),
            actionText = stringResource(R.string.signup_login_action),
            onClick = onNavigateToLogIn
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    SignUpContent(
        uiState = UiState.Initial,
        legalDocs = UiState.Initial,
        onSignUp = { _, _, _, _ -> },
        onOAuthLogin = {},
        onNavigateToLogIn = {},
        onShowDocument = { _, _ -> }
    )
}

@Preview(showBackground = true)
@Composable
fun RegisterWithErrorPreview() {
    SignUpContent(
        uiState = UiState.Initial,
        legalDocs = UiState.Initial,
        onSignUp = { _, _, _, _ -> },
        onOAuthLogin = {},
        onNavigateToLogIn = {},
        onShowDocument = { _, _ -> },
        emailError = "Невірний формат email"
    )
}

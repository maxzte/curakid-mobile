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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.ConsentType
import com.misterio.core.ui.components.CuraKidTextField
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.curakid.core.common.util.ValidationUtils

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPolicyAccepted by rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) { ValidationUtils.isValidEmail(email) }
    val isPasswordValid = remember(password) { ValidationUtils.isValidPassword(password) }
    val isFormValid = name.isNotBlank() && isEmailValid && isPasswordValid && isPolicyAccepted

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
        viewModel.signUp(email, password, name, isPolicyAccepted)
    }

    AuthScaffold {
        AuthHeader(
            title = stringResource(R.string.signup_title),
            subtitle = stringResource(R.string.signup_subtitle)
        )

        Spacer(Modifier.height(32.dp))

        // Name
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

        // Email
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

        // Password
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
                checked = isPolicyAccepted,
                onCheckedChange = { isPolicyAccepted = it }
            )
            val agreePrefix = stringResource(R.string.signup_agree_prefix)
            val termsText = stringResource(R.string.signup_terms_action)
            val andConjunction = stringResource(R.string.signup_and_conjunction)
            val privacyText = stringResource(R.string.signup_privacy_action)
            val termsTitle = stringResource(R.string.signup_terms_title)
            val privacyTitle = stringResource(R.string.signup_privacy_title)

            val annotatedString = buildAnnotatedString {
                append(agreePrefix)
                
                val linkStyle = SpanStyle(
                    color = CuraKidColors.Blue40,
                    fontWeight = FontWeight.Medium
                )
                
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "TERMS",
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = {
                            val url = (legalDocs as? UiState.Success)?.data
                                ?.find { it.type == ConsentType.TERMS }?.url
                            url?.let { onShowDocument(it, termsTitle) }
                        }
                    )
                ) {
                    append(termsText)
                }
                
                append(andConjunction)
                
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "PRIVACY",
                        styles = TextLinkStyles(style = linkStyle),
                        linkInteractionListener = {
                            val url = (legalDocs as? UiState.Success)?.data
                                ?.find { it.type == ConsentType.PRIVACY_POLICY }?.url
                            url?.let { onShowDocument(it, privacyTitle) }
                        }
                    )
                ) {
                    append(privacyText)
                }
            }
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

        GoogleButton(onTokenReceived = viewModel::oAuthLogin)

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

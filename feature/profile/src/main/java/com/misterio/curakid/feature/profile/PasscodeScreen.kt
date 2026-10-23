package com.misterio.curakid.feature.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.curakid.core.common.PasscodeMode

@Composable
fun PasscodeScreen(
    mode: PasscodeMode,
    onSuccess: () -> Unit,
    onForgotPasscode: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: PasscodeViewModel = hiltViewModel(
        creationCallback = { factory: PasscodeViewModel.Factory -> factory.create(mode) }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { BiometricHelper.findActivity(context) }
    val isBiometricAvailable = remember(context) { BiometricHelper.isBiometricAvailable(context) }

    fun triggerBiometrics() {
        activity?.let {
            BiometricHelper.showBiometricPrompt(
                activity = it,
                onSuccess = viewModel::onBiometricSuccess
            )
        }
    }

    LaunchedEffect(mode, isBiometricAvailable) {
        if (mode == PasscodeMode.UNLOCK && isBiometricAvailable) {
            triggerBiometrics()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is PasscodeEvent.Success -> onSuccess()
            }
        }
    }

    PasscodeContent(
        mode = mode,
        passcodeLength = uiState.passcode.length,
        isConfirming = uiState.isConfirming,
        errorMessage = uiState.errorMessage,
        isBiometricAvailable = isBiometricAvailable,
        onDigitClick = viewModel::onDigitClick,
        onDeleteClick = viewModel::onDeleteClick,
        onBiometricClick = ::triggerBiometrics,
        onForgotPasscode = onForgotPasscode
    )
}

@Composable
fun PasscodeContent(
    mode: PasscodeMode,
    passcodeLength: Int,
    isConfirming: Boolean,
    errorMessage: String?,
    isBiometricAvailable: Boolean,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onForgotPasscode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    val title = when (mode) {
        PasscodeMode.SETUP -> if (isConfirming) stringResource(R.string.passcode_confirm_title) else stringResource(R.string.passcode_create_title)
        PasscodeMode.UNLOCK -> stringResource(R.string.passcode_unlock_title)
        PasscodeMode.DISABLE -> stringResource(R.string.passcode_unlock_title)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < passcodeLength
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        AnimatedContent(
            targetState = errorMessage,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "error_message"
        ) { msg ->
            if (msg != null) {
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Keypad(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick,
            showBiometric = mode == PasscodeMode.UNLOCK && isBiometricAvailable,
            onBiometricClick = onBiometricClick
        )

        if (mode == PasscodeMode.UNLOCK) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.passcode_forgot),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { showResetDialog = true }
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.passcode_reset_title)) },
            text = { Text(stringResource(R.string.passcode_reset_description)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onForgotPasscode()
                    }
                ) {
                    Text(stringResource(R.string.passcode_reset_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun Keypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    showBiometric: Boolean = false,
    onBiometricClick: () -> Unit = {}
) {
    val digits = remember(showBiometric) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(if (showBiometric) "biometric" else null, "0", "delete")
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        digits.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { item ->
                    Box(
                        modifier = Modifier
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        when (item) {
                            null -> Spacer(modifier = Modifier.size(64.dp))
                            "biometric" -> {
                                IconButton(
                                    onClick = onBiometricClick,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Unlock",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            "delete" -> {
                                IconButton(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .clickable { onDigitClick(item) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PasscodeScreenPreview() {
    CurakidTheme {
        PasscodeContent(
            mode = PasscodeMode.UNLOCK,
            passcodeLength = 2,
            isConfirming = false,
            errorMessage = null,
            isBiometricAvailable = true,
            onDigitClick = {},
            onDeleteClick = {},
            onBiometricClick = {},
            onForgotPasscode = {}
        )
    }
}

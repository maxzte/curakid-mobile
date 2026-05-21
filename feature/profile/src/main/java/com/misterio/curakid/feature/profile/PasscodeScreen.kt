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
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.misterio.curakid.core.common.PasscodeMode
import com.misterio.curakid.core.common.SecurityManager
import kotlinx.coroutines.delay

@Composable
fun PasscodeScreen(
    mode: PasscodeMode,
    securityManager: SecurityManager,
    onSuccess: () -> Unit,
    onForgotPasscode: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val title = when (mode) {
        PasscodeMode.SETUP -> if (isConfirming) "Підтвердіть ПІН-код" else "Встановіть ПІН-код"
        PasscodeMode.UNLOCK -> "Введіть ПІН-код"
        PasscodeMode.DISABLE -> "Введіть ПІН-код для вимкнення"
    }

    LaunchedEffect(passcode) {
        if (passcode.length == 4) {
            when (mode) {
                PasscodeMode.SETUP -> {
                    if (!isConfirming) {
                        confirmPasscode = passcode
                        passcode = ""
                        isConfirming = true
                    } else {
                        if (passcode == confirmPasscode) {
                            securityManager.savePasscode(passcode)
                            onSuccess()
                        } else {
                            errorMessage = "Коди не збігаються"
                            delay(1000)
                            passcode = ""
                            confirmPasscode = ""
                            isConfirming = false
                            errorMessage = null
                        }
                    }
                }
                PasscodeMode.UNLOCK -> {
                    if (securityManager.verifyPasscode(passcode)) {
                        onSuccess()
                    } else {
                        errorMessage = "Неправильний ПІН-код"
                        delay(1000)
                        passcode = ""
                        errorMessage = null
                    }
                }
                PasscodeMode.DISABLE -> {
                    if (securityManager.verifyPasscode(passcode)) {
                        securityManager.clearPasscode()
                        onSuccess()
                    } else {
                        errorMessage = "Неправильний ПІН-код"
                        delay(1000)
                        passcode = ""
                        errorMessage = null
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
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
                val isFilled = index < passcode.length
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
            onDigitClick = { if (passcode.length < 4) passcode += it },
            onDeleteClick = { if (passcode.isNotEmpty()) passcode = passcode.dropLast(1) }
        )

        if (mode == PasscodeMode.UNLOCK) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Забули ПІН-код?",
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
            title = { Text("Скидання ПІН-коду") },
            text = { Text("Якщо ви забули ПІН-код, вам доведеться вийти з акаунта. Це видалить ПІН-код та всі локальні дані сесії. Ви зможете увійти знову за допомогою свого пароля.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onForgotPasscode()
                    }
                ) {
                    Text("Вийти та скинути")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun Keypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(null, "0", "delete")
    )

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
                            "delete" -> {
                                IconButton(
                                    onClick = onDeleteClick,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Backspace,
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

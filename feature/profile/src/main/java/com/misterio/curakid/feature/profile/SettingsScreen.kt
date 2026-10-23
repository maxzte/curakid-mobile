package com.misterio.curakid.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.curakid.core.common.PasscodeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPasscode: (PasscodeMode) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsStateWithLifecycle()
    val isPasscodeEnabled by viewModel.isPasscodeEnabled.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    SettingsContent(
        isNotificationsEnabled = isNotificationsEnabled,
        isPasscodeEnabled = isPasscodeEnabled,
        showDeleteDialog = showDeleteDialog,
        deleteState = deleteState,
        onNotificationsChange = viewModel::setNotificationsEnabled,
        onShowDeleteDialogChange = viewModel::setShowDeleteDialog,
        onDeleteAccount = viewModel::deleteAccount,
        onNavigateToPasscode = onNavigateToPasscode,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    isNotificationsEnabled: Boolean,
    isPasscodeEnabled: Boolean,
    showDeleteDialog: Boolean,
    deleteState: UiState<Unit>,
    onNotificationsChange: (Boolean) -> Unit,
    onShowDeleteDialogChange: (Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigateToPasscode: (PasscodeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onShowDeleteDialogChange(false) },
            title = { Text(stringResource(R.string.delete_profile_title)) },
            text = { Text(stringResource(R.string.delete_profile_warning)) },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAccount,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onShowDeleteDialogChange(false) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(CurakidTheme.paddings.medium)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.notifications),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.notifications_description),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isNotificationsEnabled,
                    onCheckedChange = onNotificationsChange
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = CurakidTheme.colors.outlineVariant)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.passcode_protection),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.passcode_protection_description),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isPasscodeEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            onNavigateToPasscode(PasscodeMode.SETUP)
                        } else {
                            onNavigateToPasscode(PasscodeMode.DISABLE)
                        }
                    }
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = CurakidTheme.colors.outlineVariant)
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { onShowDeleteDialogChange(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                enabled = deleteState !is UiState.Loading
            ) {
                if (deleteState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).height(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(stringResource(R.string.delete_profile))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.app_version, "1.0.0"),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CurakidTheme {
        SettingsContent(
            isNotificationsEnabled = true,
            isPasscodeEnabled = false,
            showDeleteDialog = false,
            deleteState = UiState.Initial,
            onNotificationsChange = {},
            onShowDeleteDialogChange = {},
            onDeleteAccount = {},
            onNavigateToPasscode = {},
            onBack = {}
        )
    }
}

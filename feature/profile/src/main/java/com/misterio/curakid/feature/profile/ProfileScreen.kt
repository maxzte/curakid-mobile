package com.misterio.curakid.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.User
import com.misterio.core.ui.components.CuraKidButton
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun ProfileScreen(
    onAddChild: () -> Unit = {},
    onSettingsOpen: () -> Unit = {},
    onChatClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val children by viewModel.children.collectAsStateWithLifecycle()
    val selectedChild by viewModel.selectedChild.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    ProfileContent(
        children = children,
        selectedChild = selectedChild,
        currentUser = currentUser,
        onChildSelected = viewModel::onChildSelected,
        onUpdateChild = viewModel::updateChild,
        onUpdateUserName = viewModel::updateUserName,
        onLogout = viewModel::logout,
        onAddChild = onAddChild,
        onSettingsOpen = onSettingsOpen,
        onChatClick = onChatClick
    )
}

@Composable
fun ProfileContent(
    children: List<Child>,
    selectedChild: Child?,
    currentUser: User?,
    onChildSelected: (UUID) -> Unit,
    onUpdateChild: (Child) -> Unit,
    onUpdateUserName: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    onAddChild: () -> Unit = {},
    onSettingsOpen: () -> Unit = {},
    onChatClick: () -> Unit = {},
) {
    var activeModal by remember { mutableStateOf<MedicalCategory?>(null) }

    activeModal?.let { category ->
        selectedChild?.let { child ->
            MedicalPickerModal(
                category = category,
                selectedItems = child.getMedicalItems(category),
                onDismiss = { activeModal = null },
                onConfirm = { updated ->
                    onUpdateChild(child.withMedicalItems(category, updated))
                    activeModal = null
                }
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = rememberListContentPadding(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
    ) {
        itemWrapper {
            ScreenHeader(
                title = stringResource(R.string.profile),
                actions = {
                    IconButton(onClick = onSettingsOpen) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "settings icon"
                        )
                    }
                }
            )
        }

        itemWrapper {
            UserCard(
                user = currentUser,
                onUpdateName = onUpdateUserName
            )
        }

        item {
            ChildSwitcher(
                children = children,
                selectedId = selectedChild?.id,
                onSelect = onChildSelected,
                onAddChild = onAddChild
            )
        }

        itemWrapper {
            selectedChild?.let {
                SelectedChildCard(
                    child = it,
                    onSave = onUpdateChild
                )
            }
        }

        selectedChild?.let { child ->
            MedicalCategory.entries.forEach { category ->
                itemWrapper {
                    MedicalSection(
                        category = category,
                        items = child.getMedicalItems(category),
                        onRemove = { item ->
                            val current = child.getMedicalItems(category)
                            onUpdateChild(child.withMedicalItems(category, current - item))
                        },
                        onAddClick = { activeModal = category }
                    )
                }
            }
        }

        itemWrapper {
            CuraKidButton(
                text = stringResource(R.string.logout),
                onClick = onLogout,
                icon = Icons.AutoMirrored.Filled.Logout,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfileScreenPreview() {
    CurakidTheme {
        ProfileContent(
            children = emptyList(),
            selectedChild = null,
            currentUser = null,
            onChildSelected = {},
            onUpdateChild = {},
            onUpdateUserName = {},
            onLogout = {},
            onAddChild = {},
            onSettingsOpen = {},
            onChatClick = {}
        )
    }
}

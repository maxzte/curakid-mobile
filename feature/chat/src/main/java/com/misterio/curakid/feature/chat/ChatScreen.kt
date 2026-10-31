@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.ui.theme.CurakidTheme
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val streamingStatusText by viewModel.streamingStatusText.collectAsStateWithLifecycle()
    val activeRedFlag by viewModel.activeRedFlag.collectAsStateWithLifecycle()
    val suggestedPrompts by viewModel.suggestedPrompts.collectAsStateWithLifecycle()

    ChatContent(
        messages = messages,
        isLoading = isLoading,
        streamingStatusText = streamingStatusText,
        activeRedFlag = activeRedFlag,
        suggestedPrompts = suggestedPrompts,
        onSendMessage = viewModel::sendMessage,
        onBack = onBack
    )
}

@Composable
fun ChatContent(
    messages: List<ChatMessageUiModel>,
    isLoading: Boolean,
    streamingStatusText: String? = null,
    activeRedFlag: Boolean = false,
    suggestedPrompts: List<String> = emptyList(),
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ChatTopBar(onBack = onBack)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            MessageList(
                messages = messages,
                isLoading = isLoading,
                listState = listState,
                streamingStatusText = streamingStatusText,
                activeRedFlag = activeRedFlag,
                suggestedPrompts = suggestedPrompts,
                onSuggestedPromptClick = onSendMessage
            )

            ScrollToBottomFAB(
                isVisible = { showFabState.value },
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, end = 16.dp)
            )
        }

        ChatInputSection(
            isLoading = isLoading,
            onSendMessage = onSendMessage,
            suggestedPrompts = if (messages.isNotEmpty()) suggestedPrompts else emptyList()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    CurakidTheme {
        ChatContent(
            messages = listOf(
                ChatMessageUiModel(
                    id = UUID.randomUUID(),
                    childId = UUID.randomUUID(),
                    userText = "Привіт, як здоров'я дитини?",
                    aiText = "",
                    isStreaming = false,
                    createdAt = "12:00",
                    parsedAstNode = null
                ),
                ChatMessageUiModel(
                    id = UUID.randomUUID(),
                    childId = UUID.randomUUID(),
                    userText = "",
                    aiText = "Вітаю! Все добре. Чим можу допомогти?",
                    isStreaming = false,
                    createdAt = "12:01",
                    parsedAstNode = null
                )
            ),
            isLoading = false,
            onSendMessage = {},
            onBack = {}
        )
    }
}

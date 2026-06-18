@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.string.RichTextStringStyle
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun ScrollToBottomFAB(
    isVisible: () -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible(),
        modifier = modifier,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = CurakidTheme.colors.primary
            ),
            shape = CircleShape,
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Scroll to bottom",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ChatScreen(
    onBack: () -> Unit,
) {
    val viewModel: ChatViewModel = hiltViewModel()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val showFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Column(
        modifier = Modifier
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
                viewModel = viewModel,
                listState = listState
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
            viewModel = viewModel
        )
    }
}

@Composable
fun ChatTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
        Text(
            "ШІ-Асистент",
            style = CurakidTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun MessageList(
    viewModel: ChatViewModel,
    listState: LazyListState
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val reversedMessages = remember(messages) { messages.reversed() }

    if (isLoading && messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CurakidTheme.colors.primary)
        }
    }

    if (messages.isEmpty() && !isLoading) {
        Text(
            text = "Вітаємо!\nЩо Вас цікавить?",
            modifier = Modifier
                .padding(vertical = 100.dp, horizontal = CurakidTheme.paddings.medium)
        )
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
        reverseLayout = true
    ) {
        if (isLoading && reversedMessages.firstOrNull()?.userText.orEmpty().isNotEmpty()) {
            item(key = "typing_indicator") {
                TypingIndicatorBubble()
            }
        }

        reversedMessages.forEach { message ->
            val isUser = message.userText.isNotEmpty()
            val parsedNode = message.parsedAstNode
            val isStreaming = message.isStreaming

            if (!isUser && !isStreaming && parsedNode != null) {
                renderMarkdownLazy(parsedNode, message.id)
            } else {
                item(
                    key = message.id,
                    contentType = if (isUser) "user" else "ai_streaming"
                ) {
                    MessageBubble(message = message)
                }
            }
        }
    }
}

/**
 * Розбиває Markdown вузол на окремі елементи LazyColumn
 */
fun LazyListScope.renderMarkdownLazy(parsedNode: StableAstNode, messageId: UUID) {
    val nodes = parsedNode.nodes
    nodes.forEachIndexed { index, node ->
        item(
            key = "${messageId}_$index",
            contentType = "markdown_block"
        ) {
            RichText(modifier = Modifier.padding(CurakidTheme.paddings.small)) {
                BasicMarkdown(astNode = node)
            }
        }
    }
}

@Composable
fun ChatInputSection(
    viewModel: ChatViewModel
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    ChatInput(
        text = inputText,
        onTextChange = { inputText = it },
        onSend = {
            viewModel.sendMessage(inputText)
            inputText = ""
        },
        isLoading = isLoading,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun MessageBubble(
    message: ChatMessageUiModel,
    modifier: Modifier = Modifier
) {
    val isUser = remember(message.userText) { message.userText.isNotEmpty() }
    val shape = remember(isUser) {
        if (isUser) {
            RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
        } else {
            RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        }
    }
    val bubbleAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor =
        if (isUser) CurakidTheme.colors.primary else CurakidTheme.colors.surfaceVariant
    val contentColor = if (isUser) Color.White else CurakidTheme.colors.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = bubbleAlignment
    ) {
        Surface(
            shape = shape,
            color = containerColor,
            contentColor = contentColor
        ) {
            Box(
                modifier = Modifier.padding(CurakidTheme.paddings.small)
            ) {
                if (isUser) {
                    Text(text = message.userText)
                } else {
                    // Тільки для стрімінгу або якщо нема розпарсеного нода
                    Text(text = message.aiText)
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    modifier: Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
        ),
        modifier = modifier
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .navigationBarsPadding()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Запитайте щось...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                maxLines = 4
            )
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Відправити",
                    tint = if (text.isNotBlank() && !isLoading) CurakidTheme.colors.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
    val d1 by animatedOffset(0)
    val d2 by animatedOffset(150)
    val d3 by animatedOffset(300)

    val density = LocalDensity.current

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Surface(
            color = CurakidTheme.colors.surfaceVariant,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                @Composable
                fun Dot(offset: Float) {

                    val px = with(density) { offset.dp.toPx() }

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = px
                            }
                            .size(6.dp)
                            .background(
                                CurakidTheme.colors.onSurfaceVariant,
                                CircleShape
                            )
                    )
                }

                Dot(d1)
                Dot(d2)
                Dot(d3)
            }
        }
    }
}

@Composable
fun animatedOffset(delay: Int) = rememberInfiniteTransition(label = "dot").animateFloat(
    initialValue = 0f,
    targetValue = -6f,
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = 300,
            easing = EaseInOut
        ),
        repeatMode = RepeatMode.Reverse,
        initialStartOffset = StartOffset(delay)
    ),
    label = "offset"
)

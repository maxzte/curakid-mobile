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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.material3.RichText
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

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
fun MessageList(
    messages: List<ChatMessageUiModel>,
    isLoading: Boolean,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val reversedMessages = remember(messages) { messages.reversed() }

    if (isLoading && messages.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = CurakidTheme.colors.primary)
        }
    }

    if (messages.isEmpty() && !isLoading) {
        Text(
            text = stringResource(R.string.chat_welcome_title),
            modifier = modifier
                .padding(vertical = 100.dp, horizontal = CurakidTheme.paddings.medium)
        )
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
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
                    Text(text = message.aiText)
                }
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

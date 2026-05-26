@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.BasicRichText
import com.misterio.core.domain.model.Message
import com.misterio.core.ui.theme.CurakidTheme


@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ШІ-Асистент",
                        style = CurakidTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CurakidTheme.colors.surface,
                    titleContentColor = CurakidTheme.colors.onSurface,
                    navigationIconContentColor = CurakidTheme.colors.onSurface
                )
            )
        },
        bottomBar = {
            ChatInput(
                text = inputText,
                onTextChange = { inputText = it },
                onSend = {
                    viewModel.sendMessage(inputText)
//                    viewModel.sendTestMessage(inputText)
                    inputText = ""
                },
                isLoading = isLoading
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                messages,
                key = { it.id }
            ) { message ->
                MessageBubble(message)
            }

            if (isLoading && messages.lastOrNull()?.isFromUser == true) {
                item {
                    TypingIndicatorBubble()
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    // Колір фону та тексту залежно від автора
    val containerColor = if (message.isFromUser) {
        CurakidTheme.colors.onSecondaryContainer
    } else {
        CurakidTheme.colors.secondaryContainer
    }
    val contentColor = if (message.isFromUser) {
        Color.White
    } else {
        CurakidTheme.colors.onSurfaceVariant
    }

    // Текст користувача вирівнюємо по правому краю, ШІ — по лівому
    val textAlign = if (message.isFromUser) {
        TextAlign.End
    } else {
        TextAlign.Start
    }

    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }
    val bubbleAlignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart
//    val parser = remember { CommonmarkAstNodeParser() }
//    val astNode = remember(message.text) {
//        parser.parse(message.text.trimIndent())
//    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = bubbleAlignment
    ) {
        Surface(
//            color = containerColor,
//            contentColor = contentColor,
            shape = shape,
        ) {
            Box(
                modifier = Modifier.padding(CurakidTheme.paddings.small)
            ) {
                if (message.isStreaming) {
                    AnimatedContent(
                        targetState = message.text,
                        transitionSpec = {
                            fadeIn(tween(150)) togetherWith fadeOut(tween(0))
                        },
                        label = "StreamingText"
                    ) { text ->
                        Text(text = text)
                    }
                } else {
                    val parser = remember { CommonmarkAstNodeParser() }
                    val astNode = remember(message.text) {
                        parser.parse(message.text.trimIndent())
                    }
                    BasicRichText(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        BasicMarkdown(astNode = astNode)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .drawBehind {
                    val strokeWidth = 0.5.dp.toPx()
                    val y = strokeWidth / 2
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                }
                .padding(8.dp)
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
fun TypingIndicatorBubble() {
    val d1 by animatedOffset(0)
    val d2 by animatedOffset(150)
    val d3 by animatedOffset(300)

    val density = LocalDensity.current

    Box(
        modifier = Modifier.fillMaxWidth(),
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

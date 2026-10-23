@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.signup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.BasicRichText
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.theme.CurakidTheme

@Composable
fun LegalDocumentScreen(
    url: String,
    title: String,
    onBack: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val documentContent by viewModel.documentContent.collectAsStateWithLifecycle()

    LaunchedEffect(url) {
        viewModel.downloadDocument(url)
    }

    LegalDocumentContent(
        title = title,
        documentContent = documentContent,
        onBack = onBack
    )
}

@Composable
fun LegalDocumentContent(
    title: String,
    documentContent: UiState<String>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = documentContent) {
                is UiState.Loading -> {
                    LoadingScreen()
                }
                is UiState.Success -> {
                    BasicRichText(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Markdown(content = state.data)
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = stringResource(R.string.legal_document_error_prefix, state.errorMessage),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> Unit
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LegalDocumentPreview() {
    CurakidTheme {
        LegalDocumentContent(
            title = "Політика конфіденційності",
            documentContent = UiState.Success("# Заголовок\n\nТекст політики конфіденційності."),
            onBack = {}
        )
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.ui.BasicRichText
import com.misterio.core.domain.UiState
import com.misterio.core.ui.components.ErrorView
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun AiReportScreen(
    reportId: UUID,
    onBack: () -> Unit,
    viewModel: AiReportViewModel = hiltViewModel(
        creationCallback = { factory: AiReportViewModel.Factory ->
            factory.create(reportId)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recommendations)) },
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
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is UiState.Loading, UiState.Initial -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.ai_analyzing_child_data),
                            style = CurakidTheme.typography.bodyMedium,
                            color = CurakidTheme.colors.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(color = CurakidTheme.colors.primary)
                    }
                }

                is UiState.Success -> {
                    val (report, recommendations) = state.data
                    val parser = remember { CommonmarkAstNodeParser() }
                    val astNodeReport = remember(report) {
                        parser.parse(report.trimIndent())
                    }
                    val astNodeRec = remember(recommendations) {
                        parser.parse(recommendations.trimIndent())
                    }

                    AiReportContent(
                        astNodeReport = astNodeReport,
                        astNodeRec = astNodeRec
                    )
                }

                is UiState.Error -> {
                    ErrorView(
                        error = state.error,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@Composable
fun AiReportContent(
    astNodeReport: AstNode,
    astNodeRec: AstNode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium)
    ) {
        BasicRichText {
            BasicMarkdown(astNode = astNodeReport)
            BasicMarkdown(astNode = astNodeRec)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiReportPreview() {
    CurakidTheme {
        val parser = CommonmarkAstNodeParser()
        AiReportContent(
            astNodeReport = parser.parse("# Звіт про розвиток\nДитина розвивається відповідно до вікових норм."),
            astNodeRec = parser.parse("## Рекомендації\nПродовжуйте щоденні вправи на дрібну моторику.")
        )
    }
}

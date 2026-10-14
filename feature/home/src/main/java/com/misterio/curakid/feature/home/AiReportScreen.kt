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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.BasicRichText
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.ui.theme.CurakidTheme
import java.util.UUID

@Composable
fun AiReportScreen(
    reportId: UUID,
    onBack: () -> Unit
) {
    val viewModel: AiReportViewModel = hiltViewModel(
        creationCallback = { factory: AiReportViewModel.Factory ->
            factory.create(reportId)
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Рекомендації") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            when (uiState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AI is analyzing your child's data...")
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        // Here would be the shimmer effect
                    }
                }

                is UiState.Success -> {
                    val report = (uiState as UiState.Success).data.first
                    val recommendations = (uiState as UiState.Success).data.second
                    val parser = remember { CommonmarkAstNodeParser() }
                    val astNodeReport = remember(report) {
                        parser.parse(report.trimIndent())
                    }
                    val astNodeRec = remember(recommendations) {
                        parser.parse(recommendations.trimIndent())
                    }

                    Column(
                        modifier = Modifier
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

                is UiState.Error -> {
                    val error = (uiState as UiState.Error).errorMessage
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }

                else -> {}
            }
        }
    }
}

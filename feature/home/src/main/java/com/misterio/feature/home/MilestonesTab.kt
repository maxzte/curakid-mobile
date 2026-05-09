package com.misterio.feature.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.ui.components.MilestoneItem
import com.misterio.core.domain.model.Milestone
import com.misterio.core.domain.model.MilestoneDomain

@Composable
fun MilestonesTab(
    childId: String,
    viewModel: MilestonesViewModel = hiltViewModel()
) {
    val milestones by viewModel.milestones(childId).collectAsState()

    val groupedMilestones = milestones.groupBy { it.domain }

    LazyColumn {
        MilestoneDomain.values().forEach { domain ->
            val domainMilestones = groupedMilestones[domain] ?: emptyList()
            if (domainMilestones.isNotEmpty()) {
                item {
                    Text(
                        text = domain.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(domainMilestones) { milestone ->
                    MilestoneItem(
                        text = milestone.text,
                        isAchieved = milestone.isAchieved,
                        onToggle = { viewModel.toggleMilestone(childId, milestone) }
                    )
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

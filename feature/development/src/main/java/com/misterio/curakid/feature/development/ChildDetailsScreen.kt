@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.AiReportCard
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.development.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import java.time.temporal.ChronoUnit

@Composable
fun ChildDetailsScreen(
    onBack: () -> Unit,
    onViewAiReport: () -> Unit
) {
    val viewModel: ChildDetailsViewModel = hiltViewModel()

    val childState by viewModel.selectedChild.collectAsState()
    val measurementsState by viewModel.measurements.collectAsState()
    val lastReport by viewModel.lastAiReport.collectAsState()

    val child = childState.getOrNull()
    val measurements = measurementsState.getOrNull() ?: emptyList()

    if (childState is UiState.Loading || measurementsState is UiState.Loading) {
        LoadingScreen()
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    var selectedChartType by remember { mutableStateOf(GrowthChartType.WeightForAge) }

    var showActionSelectionSheet by remember { mutableStateOf(false) }
    var showAddMeasurementSheet by remember { mutableStateOf(false) }
    var showAddHeadCircSheet by remember { mutableStateOf(false) }

    if (showActionSelectionSheet) {
        MeasurementActionSelectionSheet(
            onDismiss = { showActionSelectionSheet = false },
            onWeightHeightClick = {
                showActionSelectionSheet = false
                showAddMeasurementSheet = true
            },
            onHeadCircClick = {
                showActionSelectionSheet = false
                showAddHeadCircSheet = true
            }
        )
    }

    if (showAddMeasurementSheet && child != null) {
        AddMeasurementBottomSheet(
            child = child,
            initialWeight = measurements.firstOrNull { it.weight != null }?.weight,
            initialHeight = measurements.firstOrNull { it.height != null }?.height,
            onDismiss = { showAddMeasurementSheet = false },
            onDoneClick = { weight, height ->
                viewModel.addMeasurement(weight, height, null)
                showAddMeasurementSheet = false
            }
        )
    }

    if (showAddHeadCircSheet && child != null) {
        AddHeadCircBottomSheet(
            childName = child.name,
            prevHeadCirc = measurements.firstOrNull { it.headCirc != null }?.headCirc,
            onDismiss = { showAddHeadCircSheet = false },
            onDoneClick = { headCirc ->
                viewModel.addMeasurement(null, null, headCirc)
                showAddHeadCircSheet = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        GrowthTab(
            child = child,
            measurements = measurements,
            lastReport = lastReport,
            modelProducer = modelProducer,
            selectedChartType = selectedChartType,
            onChartTypeChange = { selectedChartType = it },
            onViewAiReportClick = onViewAiReport,
            onAddMeasurementClick = { showActionSelectionSheet = true }
        )
    }
}

@Composable
fun GrowthTab(
    child: Child?,
    measurements: List<Measurement>,
    lastReport: MeasurementAiReport?,
    modelProducer: CartesianChartModelProducer,
    selectedChartType: GrowthChartType,
    onChartTypeChange: (GrowthChartType) -> Unit,
    onViewAiReportClick: () -> Unit,
    onAddMeasurementClick: () -> Unit
) {
    TrackFab(show = true, icon = Icons.Default.Add, onClick = onAddMeasurementClick)

    val lineStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(CurakidTheme.colors.primary)),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(
                Brush.verticalGradient(
                    listOf(CurakidTheme.colors.primary.copy(alpha = 0.4f), Color.Transparent)
                )
            )
        ),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            style = TextStyle(color = Color.White, fontSize = 12.sp),
            background = rememberShapeComponent(
                fill = Fill(Color.Black),
                shape = RoundedCornerShape(50)
            ),
            padding = Insets(horizontal = 8.dp, vertical = 4.dp),
            margins = Insets(all = 4.dp),
        ),
        indicator = { color ->
            ShapeComponent(
                fill = Fill(Color.White),
                shape = CircleShape,
                strokeFill = Fill(color),
                strokeThickness = 2.dp
            )
        },
        indicatorSize = 6.dp,
        guideline = rememberLineComponent(Fill(Color.LightGray.copy(alpha = 0.5f))),
    )

    val chartData = remember(measurements, selectedChartType) {
        when (selectedChartType) {
            GrowthChartType.WeightForAge -> measurements.filter { it.weight != null }
                .sortedBy { it.date }

            GrowthChartType.HeightForAge -> measurements.filter { it.height != null }
                .sortedBy { it.date }

            GrowthChartType.WeightForHeight -> measurements.filter { it.weight != null && it.height != null }
                .sortedBy { it.height }

            GrowthChartType.BMI -> measurements.filter { it.weight != null && it.height != null }
                .sortedBy { it.date }

            GrowthChartType.HeadCircumference -> measurements.filter { it.headCirc != null }
                .sortedBy { it.date }
        }
    }

    LaunchedEffect(chartData, child) {
        if (chartData.isNotEmpty() && child != null) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = chartData.map { m ->
                            if (selectedChartType == GrowthChartType.WeightForHeight) {
                                m.height!!
                            } else {
                                ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate())
                                    .toDouble() / 30.44
                            }
                        },
                        y = chartData.map { m ->
                            when (selectedChartType) {
                                GrowthChartType.WeightForAge -> m.weight!!
                                GrowthChartType.HeightForAge -> m.height!!
                                GrowthChartType.WeightForHeight -> m.weight!!
                                GrowthChartType.BMI -> {
                                    val weight = m.weight!!
                                    val height = m.height!!
                                    weight / ((height / 100) * (height / 100))
                                }

                                GrowthChartType.HeadCircumference -> m.headCirc!!
                            }
                        }
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
        contentPadding = rememberListContentPadding()
    ) {
        item {
            Column {
                ScreenHeader(title = "Заміри", childName = child?.name ?: "")

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(GrowthChartType.entries.toTypedArray()) { type ->
                        FilterChip(
                            selected = selectedChartType == type,
                            onClick = { onChartTypeChange(type) },
                            label = { Text(type.label) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (chartData.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_data_yet), color = Color.Gray)
                    }
                } else {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(lineProvider = { _, _ -> lineStyle }),
                            startAxis = VerticalAxis.rememberStart(
                                guideline = rememberLineComponent(
                                    fill = Fill(Color.LightGray.copy(alpha = 0.2f)),
                                    thickness = 1.dp,
                                    shape = DashedShape(dashLength = 4.dp, gapLength = 2.dp)
                                )
                            ),
                            bottomAxis = HorizontalAxis.rememberBottom(guideline = null),
                            marker = marker
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = lastReport != null,
                enter = expandVertically() + fadeIn()
            ) {
                AiReportCard(
                    title = "Аналіз останнього заміру",
                    reportText = lastReport?.text ?: "",
                    recommendations = lastReport?.recommendations ?: "",
                )
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.measurement_history),
                actionLabel = ""
            ) { }
        }


        items(measurements) { measurement ->
            MeasurementItem(measurement)
        }
    }
}

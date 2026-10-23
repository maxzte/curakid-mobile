@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.misterio.core.domain.UiState
import com.misterio.core.domain.model.ChartNormPoint
import com.misterio.core.domain.model.Child
import com.misterio.core.domain.model.Measurement
import com.misterio.core.domain.model.MeasurementAiReport
import com.misterio.core.ui.TrackFab
import com.misterio.core.ui.components.AiReportCard
import com.misterio.core.ui.components.LoadingScreen
import com.misterio.core.ui.components.ScreenHeader
import com.misterio.core.ui.components.SectionHeader
import com.misterio.core.ui.components.itemWrapper
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Composable
fun ChildDetailsScreen(
    lifePeriods: List<String>,
    onBack: () -> Unit = {},
    onViewAiReport: () -> Unit = {}
) {
    val viewModel: ChildDetailsViewModel = hiltViewModel()

    val childState by viewModel.selectedChild.collectAsStateWithLifecycle()
    val measurementsState by viewModel.measurements.collectAsStateWithLifecycle()
    val lastReport by viewModel.lastAiReport.collectAsStateWithLifecycle()
    val reportLoading by viewModel.reportLoading.collectAsStateWithLifecycle()
    val norms by viewModel.norms.collectAsStateWithLifecycle()
    val selectedChartType by viewModel.selectedChartType.collectAsStateWithLifecycle()

    val child = childState.getOrNull()
    val measurements = measurementsState.getOrNull() ?: emptyList()

    if (childState is UiState.Loading || measurementsState is UiState.Loading) {
        LoadingScreen()
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

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
            weightNorm = viewModel.getNormRange(GrowthChartType.WeightForAge),
            heightNorm = viewModel.getNormRange(GrowthChartType.HeightForAge),
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
            normRange = viewModel.getNormRange(GrowthChartType.HeadCircumference),
            onDismiss = { showAddHeadCircSheet = false },
            onDoneClick = { headCirc ->
                viewModel.addMeasurement(null, null, headCirc)
                showAddHeadCircSheet = false
            }
        )
    }

    ScreenContent(
        child = child,
        measurements = measurements,
        norms = norms,
        lastReport = lastReport,
        reportLoading = reportLoading,
        modelProducer = modelProducer,
        selectedChartType = selectedChartType,
        lifePeriods = lifePeriods,
        onChartTypeChange = { viewModel.onChartTypeChange(it) },
        onAddMeasurementClick = { showActionSelectionSheet = true }
    )
}

@Composable
private fun ScreenContent(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    lastReport: MeasurementAiReport?,
    reportLoading: Boolean,
    modelProducer: CartesianChartModelProducer,
    selectedChartType: GrowthChartType,
    lifePeriods: List<String>,
    onChartTypeChange: (GrowthChartType) -> Unit,
    onAddMeasurementClick: () -> Unit
) {
    TrackFab(show = true, icon = Icons.Default.Add, onClick = onAddMeasurementClick)

    val currentAgeMonths = remember(child) {
        if (child == null) 0.0
        else ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44
    }

    val autoPeriodLabel = remember(currentAgeMonths, lifePeriods) {
        lifePeriods.find { label ->
            val period = GrowthChartCalculator.parsePeriod(label, lifePeriods)
            currentAgeMonths >= period.start && currentAgeMonths <= period.end
        } ?: "0-72"
    }

    val maxAgeMonths = 72.0
    val chartData = remember(measurements, selectedChartType, child, autoPeriodLabel, lifePeriods) {
        val baseData = when (selectedChartType) {
            GrowthChartType.WeightForAge -> measurements.filter { it.weight != null }
            GrowthChartType.HeightForAge -> measurements.filter { it.height != null }
            GrowthChartType.WeightForHeight -> measurements.filter { it.weight != null && it.height != null }
            GrowthChartType.BMI -> measurements.filter { it.weight != null && it.height != null }
            GrowthChartType.HeadCircumference -> measurements.filter { it.headCirc != null }
        }

        if (child == null) return@remember emptyList<Measurement>()

        val filtered = if (selectedChartType == GrowthChartType.WeightForHeight) {
            baseData.filter { m ->
                val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate()).toDouble()
                days / 30.44 <= maxAgeMonths
            }
        } else {
            val period = GrowthChartCalculator.parsePeriod(autoPeriodLabel, lifePeriods)
            baseData.filter { m ->
                val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate()).toDouble()
                val month = days / 30.44
                month >= period.start && month <= period.end
            }
        }

        when (selectedChartType) {
            GrowthChartType.WeightForHeight -> filtered.sortedBy { it.height }
            else -> filtered.sortedBy { it.date }
        }
    }

    var selectedMarkerX by remember { mutableStateOf<Double?>(null) }
    var chartRangeY by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var hasNormsDrawn by remember { mutableStateOf(false) }

    LaunchedEffect(selectedChartType) {
        chartRangeY = null
        hasNormsDrawn = false
        selectedMarkerX = null
    }

    val childMeasurementByX = remember(chartData, selectedChartType, child, autoPeriodLabel, lifePeriods) {
        if (child == null) {
            emptyMap()
        } else {
            val isWfh = selectedChartType == GrowthChartType.WeightForHeight
            val period = GrowthChartCalculator.parsePeriod(autoPeriodLabel, lifePeriods)
            val xUnitScale = GrowthChartCalculator.getXScale(isWfh, period)

            chartData.associateBy { m ->
                if (isWfh) {
                    m.height ?: 0.0
                } else {
                    val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate()).toDouble()
                    GrowthChartCalculator.roundForChart((days - (period.start * 30.44)) / xUnitScale)
                }
            }
        }
    }

    val selectedMeasurement = selectedMarkerX?.let { childMeasurementByX[it] }

    val markerVisibilityListener = remember(childMeasurementByX) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x ?: return
                selectedMarkerX = childMeasurementByX.keys.minByOrNull { abs(it - x) }
                    ?.takeIf { abs(it - x) < 0.1 }
            }

            override fun onUpdated(marker: CartesianMarker, targets: List<CartesianMarker.Target>) {
                val x = targets.firstOrNull()?.x ?: return
                selectedMarkerX = childMeasurementByX.keys.minByOrNull { abs(it - x) }
                    ?.takeIf { abs(it - x) < 0.1 }
            }

            override fun onHidden(marker: CartesianMarker) {
                selectedMarkerX = null
            }
        }
    }

    var chartReady by remember(selectedChartType) { mutableStateOf(false) }
    val chartJob = remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    DisposableEffect(chartData, child, norms, selectedChartType, autoPeriodLabel, lifePeriods) {
        chartJob.value?.cancel()
        chartReady = false

        chartJob.value = scope.launch {
            if (child == null || norms.isEmpty()) return@launch
            val capturedType = selectedChartType

            val result = withContext(Dispatchers.Default) {
                GrowthChartCalculator.computeChartData(
                    child = child,
                    chartData = chartData,
                    norms = norms,
                    chartType = capturedType,
                    autoPeriodLabel = autoPeriodLabel,
                    lifePeriods = lifePeriods
                )
            }

            if (isActive && capturedType == selectedChartType) {
                chartRangeY = (result.minY * 0.95) to (result.maxY * 1.05)
                hasNormsDrawn = result.filteredNorms.isNotEmpty()

                modelProducer.runTransaction {
                    lineSeries {
                        if (result.filteredNorms.isNotEmpty()) {
                            val normXValues = result.filteredNorms.map { it.x }
                            series(x = normXValues, y = result.filteredNorms.map { it.p3 })
                            series(x = normXValues, y = result.filteredNorms.map { it.p15 })
                            series(x = normXValues, y = result.filteredNorms.map { it.p50 })
                            series(x = normXValues, y = result.filteredNorms.map { it.p85 })
                            series(x = normXValues, y = result.filteredNorms.map { it.p97 })
                        }
                        if (result.filteredChildPoints.isNotEmpty()) {
                            series(
                                x = result.filteredChildPoints.map { it.first },
                                y = result.filteredChildPoints.map { it.second }
                            )
                        }
                    }
                }
                chartReady = true
            }
        }

        onDispose {
            chartJob.value?.cancel()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(CurakidTheme.paddings.medium),
        contentPadding = rememberListContentPadding()
    ) {
        itemWrapper {
            Column {
                ScreenHeader(title = stringResource(R.string.add_measurement), childName = child?.name.orEmpty())

                Spacer(modifier = Modifier.height(8.dp))

                TopSummaryRow(
                    child = child,
                    measurements = measurements,
                    norms = norms,
                    selectedChartType = selectedChartType
                )
            }
        }

        item {
            GrowthChartTypeChips(
                selectedType = selectedChartType,
                onTypeSelected = onChartTypeChange
            )
        }

        itemWrapper {
            GrowthChartCard(
                chartData = chartData,
                selectedChartType = selectedChartType,
                chartReady = chartReady,
                chartRangeY = chartRangeY,
                hasNormsDrawn = hasNormsDrawn,
                modelProducer = modelProducer,
                markerVisibilityListener = markerVisibilityListener
            )
        }

        itemWrapper {
            AnimatedVisibility(
                visible = selectedMeasurement != null,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                MeasurementDetailsCard(
                    measurement = selectedMeasurement,
                    child = child,
                    selectedChartType = selectedChartType
                )
            }
        }

        itemWrapper {
            AnimatedVisibility(
                visible = reportLoading || (lastReport != null && (lastReport.text.isNotBlank() || lastReport.recommendations.isNotBlank())),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                AiReportCard(
                    title = "Аналіз останнього заміру",
                    reportText = lastReport?.text.orEmpty(),
                    recommendations = lastReport?.recommendations.orEmpty(),
                    isLoading = reportLoading
                )
            }
        }

        itemWrapper {
            SectionHeader(
                title = stringResource(R.string.measurement_history),
                actionLabel = ""
            ) { }
        }

        if (measurements.isEmpty()) {
            item {
                EmptyMeasurementsState()
            }
        } else {
            items(measurements) { measurement ->
                MeasurementItem(measurement)
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

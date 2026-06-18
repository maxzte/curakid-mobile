@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

fun Double.roundForChart(): Double = String.format(Locale.US, "%.3f", this).toDouble()

private data class ChartPeriod(val start: Double, val end: Double)

private fun parsePeriod(label: String, lifePeriods: List<String>): ChartPeriod {
    return if (label.contains("-")) {
        val parts = label.split("-")
        val start = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val end = parts.getOrNull(1)?.toDoubleOrNull() ?: 72.0
        ChartPeriod(start, end)
    } else {
        val end = label.toDoubleOrNull() ?: 72.0
        val idx = lifePeriods.indexOf(label)
        val start = if (idx > 0) {
            val prevLabel = lifePeriods[idx - 1]
            if (prevLabel.contains("-")) prevLabel.split("-").last().toDoubleOrNull() ?: 0.0
            else prevLabel.toDoubleOrNull() ?: 0.0
        } else 0.0
        ChartPeriod(start, end)
    }
}

private fun getXScale(isWfh: Boolean, period: ChartPeriod): Double {
    return when {
        isWfh -> 1.0
        (period.end - period.start) <= 3.0 -> 1.0 // Days
        (period.end - period.start) <= 6.0 -> 7.0 // Weeks
        else -> 30.44 // Months
    }
}

data class ChartComputationResult(
    val filteredNorms: List<ChartNormPoint>,
    val filteredChildPoints: List<Pair<Double, Double>>,
    val minY: Double,
    val maxY: Double
)

@Composable
fun ChildDetailsScreen(
    lifePeriods: List<String>,
    onBack: () -> Unit,
    onViewAiReport: () -> Unit
) {
    onBack.hashCode() // Suppress warning, but usually this should be used in the UI
    onViewAiReport.hashCode() // Suppress warning

    val viewModel: ChildDetailsViewModel = hiltViewModel()

    val childState by viewModel.selectedChild.collectAsState()
    val measurementsState by viewModel.measurements.collectAsState()
    val lastReport by viewModel.lastAiReport.collectAsState()
    val reportLoading by viewModel.reportLoading.collectAsState()
    val normsCache by viewModel.normsCache.collectAsState()
    val norms by viewModel.n.collectAsState()
    val selectedChartType by viewModel.selectedChartType.collectAsState()

    val child = childState.getOrNull()
    val measurements = measurementsState.getOrNull() ?: emptyList()

    val currentAgeDays = remember(child) {
        if (child == null) 0.0
        else ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble()
    }

    fun getNormRange(type: GrowthChartType): ClosedFloatingPointRange<Float>? {
        val typeNorms = normsCache[type] ?: return null
        val nearest = typeNorms.minByOrNull { abs(it.x - currentAgeDays) } ?: return null
        return nearest.p15.toFloat()..nearest.p85.toFloat()
    }

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
            weightNorm = getNormRange(GrowthChartType.WeightForAge),
            heightNorm = getNormRange(GrowthChartType.HeightForAge),
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
            normRange = getNormRange(GrowthChartType.HeadCircumference),
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
        normsCache = normsCache,
        lastReport = lastReport,
        reportLoading = reportLoading,
        modelProducer = modelProducer,
        selectedChartType = selectedChartType,
        lifePeriods = lifePeriods,
        onChartTypeChange = { viewModel.onChartTypeChange(it) },
        onViewAiReportClick = onViewAiReport,
        onAddMeasurementClick = { showActionSelectionSheet = true }
    )
}

@Composable
private fun ScreenContent(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    normsCache: Map<GrowthChartType, List<ChartNormPoint>>,
    lastReport: MeasurementAiReport?,
    reportLoading: Boolean,
    modelProducer: CartesianChartModelProducer,
    selectedChartType: GrowthChartType,
    lifePeriods: List<String>,
    onChartTypeChange: (GrowthChartType) -> Unit,
    onViewAiReportClick: () -> Unit,
    onAddMeasurementClick: () -> Unit
) {
    normsCache.hashCode() // Suppress unused warning
    onViewAiReportClick.hashCode() // Suppress unused warning

    TrackFab(show = true, icon = Icons.Default.Add, onClick = onAddMeasurementClick)

    val currentAgeMonths = remember(child) {
        if (child == null) 0.0
        else ChronoUnit.DAYS.between(child.birthDate, LocalDate.now()).toDouble() / 30.44
    }

    val autoPeriodLabel = remember(currentAgeMonths, lifePeriods) {
        lifePeriods.find { label ->
            val period = parsePeriod(label, lifePeriods)
            currentAgeMonths >= period.start && currentAgeMonths <= period.end
        } ?: "0-72"
    }

    val styles = rememberGrowthChartStyles()
    val legend = rememberGrowthChartLegend(
        mainLineColor = CurakidTheme.colors.primary,
        warningLineColor = Color(0xFFFFC107),
        criticalLineColor = Color(0xFFEF5350)
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

    val axisTitleComponent = rememberTextComponent(
        style = CurakidTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        ),
        padding = Insets(all = 4.dp)
    )

    val yAxisLabel = when (selectedChartType) {
        GrowthChartType.WeightForAge -> "Вага, кг"
        GrowthChartType.HeightForAge -> "Зріст, см"
        GrowthChartType.WeightForHeight -> "Вага, кг"
        GrowthChartType.BMI -> "ІМТ"
        GrowthChartType.HeadCircumference -> "Обхват голови, см"
    }

    val xAxisLabel = when (selectedChartType) {
        GrowthChartType.WeightForHeight -> "Зріст, см"
        else -> "Вік"
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
            val period = parsePeriod(autoPeriodLabel, lifePeriods)
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

    // Reset chart state when switching chart type to prevent stale Vico internal Y-range state.
    // Without this, the old CartesianChart tries to animate with the new transaction's series
    // count but the old range map keys, causing NoSuchElementException.
    LaunchedEffect(selectedChartType) {
        Log.d(
            "ChartDebug",
            "=== selectedChartType changed to $selectedChartType, resetting state ==="
        )

        chartRangeY = null
        hasNormsDrawn = false
        selectedMarkerX = null
    }

    val childMeasurementByX =
        remember(chartData, selectedChartType, child, autoPeriodLabel, lifePeriods) {
            if (child == null) {
                emptyMap()
            } else {
                val isWfh = selectedChartType == GrowthChartType.WeightForHeight
                val period = parsePeriod(autoPeriodLabel, lifePeriods)
                val xUnitScale = getXScale(isWfh, period)

                chartData.associateBy { m ->
                    if (isWfh) {
                        m.height ?: 0.0
                    } else {
                        val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate())
                            .toDouble()
                        ((days - (period.start * 30.44)) / xUnitScale).roundForChart()
                    }
                }
            }
        }

    val selectedMeasurement = selectedMarkerX?.let { childMeasurementByX[it] }

    val markerVisibilityListener = remember(childMeasurementByX) {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>,
            ) {
                val x = targets.firstOrNull()?.x ?: return
                selectedMarkerX = childMeasurementByX.keys.minByOrNull { abs(it - x) }
                    ?.takeIf { abs(it - x) < 0.1 }
            }

            override fun onUpdated(
                marker: CartesianMarker,
                targets: List<CartesianMarker.Target>,
            ) {
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
        Log.d(
            "ChartDebug",
            "=== LaunchedEffect START, chartType=$selectedChartType, chartReady=false ==="
        )

        chartJob.value?.cancel()
        chartReady = false

        chartJob.value = scope.launch {
            if (child == null) return@launch
            if (norms.isEmpty()) return@launch
            val capturedType = selectedChartType

            val result = withContext(Dispatchers.Default) {
                val isWfh = capturedType == GrowthChartType.WeightForHeight
                val period = parsePeriod(autoPeriodLabel, lifePeriods)
                val xUnitScale = getXScale(isWfh, period)

                val filteredChildPoints = chartData.asSequence().filter { m ->
                    if (isWfh) true
                    else {
                        val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate())
                            .toDouble()
                        val mAgeMonths = days / 30.44
                        mAgeMonths >= period.start && mAgeMonths <= period.end
                    }
                }.map { m ->
                    val xValue = if (isWfh) {
                        m.height!!
                    } else {
                        val days = ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate())
                            .toDouble()
                        (days - (period.start * 30.44)) / xUnitScale
                    }
                    val yValue = getMeasurementValue(m, capturedType) ?: 0.0
                    xValue.roundForChart() to yValue
                }.sortedBy { it.first }.toList()

                val filteredNorms = norms.asSequence().filter { norm ->
                    if (isWfh) {
                        val minH = filteredChildPoints.minOfOrNull { it.first } ?: 45.0
                        val maxH = filteredChildPoints.maxOfOrNull { it.first } ?: 120.0
                        norm.x >= minH * 0.9 && norm.x <= maxH * 1.1
                    } else {
                        val mAgeMonths = norm.x / 30.44
                        mAgeMonths >= period.start && mAgeMonths <= period.end
                    }
                }.map { norm ->
                    val normX = if (isWfh) norm.x else (norm.x - (period.start * 30.44)) / xUnitScale
                    norm.copy(x = normX.roundForChart())
                }.sortedBy { it.x }.toList()

                if (filteredChildPoints.isEmpty() && filteredNorms.isEmpty()) return@withContext null

                if (!isActive) return@withContext null

                val minY = minOf(
                    filteredNorms.minOfOrNull { it.p3 } ?: Double.MAX_VALUE,
                    filteredChildPoints.minOfOrNull { it.second } ?: Double.MAX_VALUE
                ).takeIf { it != Double.MAX_VALUE } ?: 0.0

                val maxY = maxOf(
                    filteredNorms.maxOfOrNull { it.p97 } ?: Double.MIN_VALUE,
                    filteredChildPoints.maxOfOrNull { it.second } ?: Double.MIN_VALUE
                ).takeIf { it != Double.MIN_VALUE } ?: 10.0

                ChartComputationResult(filteredNorms, filteredChildPoints, minY, maxY)
            } ?: return@launch

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
                ScreenHeader(title = "Заміри", childName = child?.name ?: "")

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
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = CurakidTheme.paddings.medium),
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

        itemWrapper {
            if (chartData.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_data_yet), color = Color.Gray)
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    val chartHeight = when {
                        maxWidth < 360.dp -> 320.dp
                        maxWidth < 500.dp -> 360.dp
                        else -> 400.dp
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(chartHeight),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4FF)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFEEF7FF), Color(0xFFFFFFFF))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Log.d(
                                "ChartDebug",
                                "Render check — chartReady=$chartReady, chartRangeY=$chartRangeY, hasNormsDrawn=$hasNormsDrawn, selectedChartType=$selectedChartType"
                            )

                            if (!chartReady || chartRangeY == null) {
                                // Show a loading indicator until Y range is computed
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                key(selectedChartType) {
                                    CartesianChartHost(
                                        modifier = Modifier.fillMaxSize(),
                                        zoomState = rememberVicoZoomState(
                                            zoomEnabled = true,
                                            minZoom = Zoom.fixed(2f)
                                        ),
                                        chart = key(chartRangeY, hasNormsDrawn) {
                                            rememberCartesianChart(
                                                rememberLineCartesianLayer(
                                                    lineProvider = { seriesIndex, _, _ ->
                                                        Log.d(
                                                            "ChartDebug",
                                                            "lineProvider called: seriesIndex=$seriesIndex, hasNormsDrawn=$hasNormsDrawn"
                                                        )

                                                        val childSeriesIndex =
                                                            if (hasNormsDrawn) 5 else 0
                                                        when {
                                                            !hasNormsDrawn -> styles.childSeries
                                                            seriesIndex == childSeriesIndex -> styles.childSeries
                                                            // Norms order: p3(0), p15(1), p50(2), p85(3), p97(4)
                                                            seriesIndex == 0 -> styles.p3
                                                            seriesIndex == 1 -> styles.p15
                                                            seriesIndex == 2 -> styles.p50
                                                            seriesIndex == 3 -> styles.p85
                                                            seriesIndex == 4 -> styles.p97
                                                            else -> styles.normLine
                                                        }
                                                    },
                                                    rangeProvider = CartesianLayerRangeProvider.fixed(
                                                        minY = chartRangeY!!.first,
                                                        maxY = chartRangeY!!.second
                                                    )
                                                ),
                                                getXStep = { _, _, _ ->
                                                    if (selectedChartType == GrowthChartType.WeightForHeight) 1.0
                                                    else 1.0
                                                },
                                                markerController = CartesianMarkerController.rememberToggleOnTap(),
                                                markerVisibilityListener = markerVisibilityListener,
                                                startAxis = VerticalAxis.rememberStart(
                                                    guideline = rememberLineComponent(
                                                        fill = Fill(Color.LightGray.copy(alpha = 0.2f)),
                                                        thickness = 1.dp,
                                                        shape = DashedShape(
                                                            dashLength = 4.dp,
                                                            gapLength = 2.dp
                                                        )
                                                    ),
                                                    titleComponent = axisTitleComponent,
                                                    title = { yAxisLabel },
                                                    itemPlacer = VerticalAxis.ItemPlacer.count(count = { 6 })
                                                ),
                                                bottomAxis = HorizontalAxis.rememberBottom(
                                                    guideline = null,
                                                    titleComponent = axisTitleComponent,
                                                    title = { xAxisLabel },
                                                    itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                                                        spacing = {
                                                            if (selectedChartType == GrowthChartType.WeightForHeight) {
                                                                5
                                                            } else 4
                                                        },
                                                        addExtremeLabelPadding = true
                                                    ),
                                                    valueFormatter = { _, value, _ ->
                                                        if (selectedChartType == GrowthChartType.WeightForHeight) {
                                                            value.roundToInt().toString()
                                                        } else {
                                                            if (value % 1.0 == 0.0) {
                                                                value.toInt().toString()
                                                            } else {
                                                                String.format(
                                                                    Locale.US,
                                                                    "%.1f",
                                                                    value
                                                                )
                                                            }
                                                        }
                                                    }
                                                ),
                                                marker = marker,
                                                legend = legend,
                                            )
                                        },
                                        modelProducer = modelProducer,
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                exit = shrinkVertically() + fadeOut()
            ) {
                AiReportCard(
                    title = "Аналіз останнього заміру",
                    reportText = lastReport?.text ?: "",
                    recommendations = lastReport?.recommendations ?: "",
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

@Composable
private fun EmptyMeasurementsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .alpha(0.3f),
            tint = CurakidTheme.colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Історія замірів порожня",
            style = CurakidTheme.typography.bodyLarge,
            color = CurakidTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.text.style.TextOverflow
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
import com.misterio.core.ui.components.rememberListContentPadding
import com.misterio.core.ui.theme.CurakidTheme
import com.misterio.feature.development.R
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerVisibilityListener
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.DashedShape
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.roundForChart(): Double = String.format(Locale.US, "%.3f", this).toDouble()


@Composable
fun ChildDetailsScreen(
    lifePeriods: List<String>,
    onBack: () -> Unit,
    onViewAiReport: () -> Unit
) {
    val viewModel: ChildDetailsViewModel = hiltViewModel()

    val childState by viewModel.selectedChild.collectAsState()
    val measurementsState by viewModel.measurements.collectAsState()
    val lastReport by viewModel.lastAiReport.collectAsState()
//    val norms by viewModel.norms.collectAsState()
    val norms by viewModel.n.collectAsState()
    val selectedChartType by viewModel.selectedChartType.collectAsState()

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
            norms = norms,
            lastReport = lastReport,
            modelProducer = modelProducer,
            selectedChartType = selectedChartType,
            onChartTypeChange = { viewModel.onChartTypeChange(it) },
            onViewAiReportClick = onViewAiReport,
            onAddMeasurementClick = { showActionSelectionSheet = true }
        )
    }
}

@Composable
fun GrowthTab(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    lastReport: MeasurementAiReport?,
    modelProducer: CartesianChartModelProducer,
    selectedChartType: GrowthChartType,
    onChartTypeChange: (GrowthChartType) -> Unit,
    onViewAiReportClick: () -> Unit,
    onAddMeasurementClick: () -> Unit
) {
    TrackFab(show = true, icon = Icons.Default.Add, onClick = onAddMeasurementClick)

    val mainLineColor = CurakidTheme.colors.primary
    val normLineColor = Color.LightGray.copy(alpha = 0.5f)

    val mainLineStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(mainLineColor)),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(mainLineColor.copy(alpha = 0.16f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val normLineStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(normLineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // Additional styles for warning / critical percentile lines
    val warningLineColor = Color(0xFFFFC107) // amber-ish for p3-p15 / p85-p97 band
    val criticalLineColor = Color(0xFFEF5350) // red for extreme percentiles
    val normBandColor = Color(0xFFE8F5E9) // green-ish for p15-p85 band

    // p3 line - no area fill below it!
    val p3Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(criticalLineColor.copy(alpha = 0.5f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // p15 line - area fill between p3 and p15
    val p15Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.5f))),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(warningLineColor.copy(alpha = 0.15f))
        ),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // p50 line - area fill between p15 and p50
    val p50Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(Color.LightGray.copy(alpha = 0.8f))),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(normBandColor.copy(alpha = 0.4f))
        ),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.5.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // p85 line - area fill between p50 and p85
    val p85Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.5f))),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(normBandColor.copy(alpha = 0.4f))
        ),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // p97 line - area fill between p85 and p97
    val p97Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(criticalLineColor.copy(alpha = 0.5f))),
        areaFill = LineCartesianLayer.AreaFill.single(
            Fill(warningLineColor.copy(alpha = 0.15f))
        ),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val childPointComponent = rememberShapeComponent(
        fill = Fill(Color.White),
        shape = CircleShape,
        strokeFill = Fill(mainLineColor),
        strokeThickness = 2.dp
    )

    val childPointProvider = remember {
        LineCartesianLayer.PointProvider.single(
            LineCartesianLayer.Point(
                childPointComponent,
                size = 8.dp,
            )
        )
    }

    // Child series style with visible points
    val childSeriesStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(mainLineColor)),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(mainLineColor.copy(alpha = 0.18f))),
        pointProvider = childPointProvider,
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    // Create percentile styles that include area fills on their top series
    val p15WithBand = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.6f))),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(warningLineColor.copy(alpha = 0.12f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.25.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p85WithBand = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.4f))),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(warningLineColor.copy(alpha = 0.10f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.25.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p97WithBand = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(criticalLineColor.copy(alpha = 0.35f))),
        areaFill = LineCartesianLayer.AreaFill.single(Fill(criticalLineColor.copy(alpha = 0.10f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
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

    val labelComponent = rememberTextComponent(
        style = CurakidTheme.typography.labelSmall.copy(color = Color.Gray)
    )
    val mainIcon = rememberShapeComponent(fill = Fill(mainLineColor), shape = CircleShape)
    val normIcon = rememberShapeComponent(fill = Fill(Color(0xFFCCFFDD)), shape = CircleShape)
    val warnIcon = rememberShapeComponent(
        fill = Fill(warningLineColor.copy(alpha = 0.9f)),
        shape = CircleShape
    )
    val critIcon = rememberShapeComponent(
        fill = Fill(criticalLineColor.copy(alpha = 0.9f)),
        shape = CircleShape
    )

    val legend = rememberHorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
        items = {
            add(
                LegendItem(
                    icon = normIcon,
                    labelComponent = labelComponent,
                    label = "Норма (p15–p85)",
                )
            )
            add(
                LegendItem(
                    icon = warnIcon,
                    labelComponent = labelComponent,
                    label = "Увага (p3–p15, p85–p97)",
                )
            )
            add(
                LegendItem(
                    icon = critIcon,
                    labelComponent = labelComponent,
                    label = "Критично (<p3 або >p97)",
                )
            )
            add(
                LegendItem(
                    icon = mainIcon,
                    labelComponent = labelComponent,
                    label = "Дитина",
                )
            )
        },
        padding = Insets(top = 16.dp),
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

    val xAxisLabel =
        if (selectedChartType == GrowthChartType.WeightForHeight) "Зріст, см" else "Вік, місяці"

    val xAxisStep = if (selectedChartType == GrowthChartType.WeightForHeight) 2.0 else 0.25
    val yAxisStep = if (selectedChartType == GrowthChartType.WeightForHeight) 2.0 else 0.25
    val maxAgeMonths = 72.0
    val chartData = remember(measurements, selectedChartType, child) {
        when (selectedChartType) {
            GrowthChartType.WeightForAge -> measurements.filter {
                it.weight != null && child != null &&
                        ChronoUnit.DAYS.between(child.birthDate, it.date.toLocalDate())
                            .toDouble() / 30.44 <= maxAgeMonths
            }
                .sortedBy { it.date }

            GrowthChartType.HeightForAge -> measurements.filter {
                it.height != null && child != null &&
                        ChronoUnit.DAYS.between(child.birthDate, it.date.toLocalDate())
                            .toDouble() / 30.44 <= maxAgeMonths
            }
                .sortedBy { it.date }

            GrowthChartType.WeightForHeight -> measurements.filter { it.weight != null && it.height != null }
                .sortedBy { it.height }

            GrowthChartType.BMI -> measurements.filter {
                it.weight != null && it.height != null && child != null &&
                        ChronoUnit.DAYS.between(child.birthDate, it.date.toLocalDate())
                            .toDouble() / 30.44 <= maxAgeMonths
            }
                .sortedBy { it.date }

            GrowthChartType.HeadCircumference -> measurements.filter {
                it.headCirc != null && child != null &&
                        ChronoUnit.DAYS.between(child.birthDate, it.date.toLocalDate())
                            .toDouble() / 30.44 <= maxAgeMonths
            }
                .sortedBy { it.date }
        }
    }

    var selectedMarkerX by remember { mutableStateOf<Double?>(null) }

    val childMeasurementByX = remember(chartData, selectedChartType, child) {
        if (child == null) {
            emptyMap()
        } else {
            chartData.associateBy { m ->
                if (selectedChartType == GrowthChartType.WeightForHeight) {
                    m.height!!
                } else {
                    (ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate())
                        .toDouble() / 30.44)
                        .roundForChart()
                }
            }
        }
    }

    val selectedMeasurement = selectedMarkerX?.let { childMeasurementByX[it] }

    val markerVisibilityListener = remember {
        object : CartesianMarkerVisibilityListener {
            override fun onShown(
                marker: com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker,
                targets: List<com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker.Target>,
            ) {
                val x = targets.firstOrNull()?.x
                selectedMarkerX = x?.takeIf { childMeasurementByX.containsKey(it) }
            }

            override fun onUpdated(
                marker: com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker,
                targets: List<com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker.Target>,
            ) {
                val x = targets.firstOrNull()?.x
                selectedMarkerX = x?.takeIf { childMeasurementByX.containsKey(it) }
            }

            override fun onHidden(marker: com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker) {
                selectedMarkerX = null
            }
        }
    }


    LaunchedEffect(chartData, child, norms, selectedChartType) {
        if (child == null) return@LaunchedEffect

        withContext(Dispatchers.Default) {
            val isWfh = selectedChartType == GrowthChartType.WeightForHeight

            // 1. Готуємо координати для дитини (переводимо дні в місяці)
            val childPoints = chartData.map { m ->
                val xValue = if (isWfh) {
                    m.height!!
                } else {
                    val days =
                        ChronoUnit.DAYS.between(child.birthDate, m.date.toLocalDate()).toDouble()
                    // Переводимо в місяці та округляємо до 1 знаку (наприклад, 1.5 місяці)
                    (days / 30.44)
                }

                val yValue = when (selectedChartType) {
                    GrowthChartType.WeightForAge -> m.weight!!
                    GrowthChartType.HeightForAge -> m.height!!
                    GrowthChartType.WeightForHeight -> m.weight!!
                    GrowthChartType.BMI -> m.weight!! / (m.height!! / 100.0).pow(2.0)
                    GrowthChartType.HeadCircumference -> m.headCirc!!
                }
                xValue.roundForChart() to yValue
            }.sortedBy { it.first }

            // 2. Готуємо норми (переводимо дні в місяці та фільтруємо до 72 місяців)
            val xLimit = if (isWfh) (childPoints.maxOfOrNull { it.first } ?: 100.0) * 1.1 else 72.0

            val filteredNorms = norms.map { norm ->
                val normX = if (isWfh) norm.x else (norm.x / 30.44)
                norm.copy(x = normX.roundForChart())
            }.filter { it.x <= xLimit }.sortedBy { it.x }

            if (childPoints.isEmpty() && filteredNorms.isEmpty()) return@withContext

            // 3. Передаємо дані у Vico (кожна лінія отримує свої власні X і Y)
            modelProducer.runTransaction {
                lineSeries {
                    if (filteredNorms.isNotEmpty()) {
                        val normXValues = filteredNorms.map { it.x }

                        // Порядок ліній відповідає вашому lineProvider
                        series(x = normXValues, y = filteredNorms.map { it.p3 })
                        series(x = normXValues, y = filteredNorms.map { it.p15 })
                        series(x = normXValues, y = filteredNorms.map { it.p50 })
                        series(x = normXValues, y = filteredNorms.map { it.p85 })
                        series(x = normXValues, y = filteredNorms.map { it.p97 })
                    }

                    // Лінія дитини будується СУТО по своїх точках
                    if (childPoints.isNotEmpty()) {
                        series(
                            x = childPoints.map { it.first },
                            y = childPoints.map { it.second }
                        )
                    }
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

                // Top summary cards: Z-score, Percentile, Trend
                TopSummaryRow(
                    child = child,
                    measurements = measurements,
                    norms = norms,
                    selectedChartType = selectedChartType
                )

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
                        maxWidth < 360.dp -> 260.dp
                        maxWidth < 500.dp -> 300.dp
                        else -> 340.dp
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
                            CartesianChartHost(
                                zoomState = rememberVicoZoomState(zoomEnabled = true, minZoom = Zoom.fixed(2f)),
                                chart = rememberCartesianChart(
                                    rememberLineCartesianLayer(
                                        lineProvider = { seriesIndex, _ ->
                                            val hasNorms = norms.isNotEmpty()
                                            val childSeriesIndex = if (hasNorms) 5 else 0
                                            when {
                                                !hasNorms -> mainLineStyle
                                                seriesIndex == childSeriesIndex -> childSeriesStyle
                                                // Norms order: p3(0), p15(1), p50(2), p85(3), p97(4)
                                                seriesIndex == 0 -> p3Style
                                                seriesIndex == 1 -> p15Style
                                                seriesIndex == 2 -> p50Style
                                                seriesIndex == 3 -> p85Style
                                                seriesIndex == 4 -> p97Style
                                                else -> normLineStyle
                                            }
                                        },
                                    ),
                                    getXStep = { xAxisStep },
                                    markerController = CartesianMarkerController.rememberToggleOnTap(),
                                    markerVisibilityListener = markerVisibilityListener,
                                    startAxis = VerticalAxis.rememberStart(
                                        guideline = rememberLineComponent(
                                            fill = Fill(Color.LightGray.copy(alpha = 0.2f)),
                                            thickness = 1.dp,
                                            shape = DashedShape(dashLength = 4.dp, gapLength = 2.dp)
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
                                                    String.format(Locale.US, "%.1f", value)
                                                }
                                            }
                                        }
                                    ),
                                    marker = marker,
                                    legend = legend,
                                ),
                                modelProducer = modelProducer,
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = selectedMeasurement != null,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Деталі заміру",
                            style = CurakidTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = CurakidTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val measurement = selectedMeasurement
                        val formattedDate = measurement?.date?.toLocalDate()?.format(
                            DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        ) ?: "—"
                        val ageMonths = measurement?.let {
                            if (selectedChartType == GrowthChartType.WeightForHeight) {
                                "${it.height?.roundToInt() ?: 0} см"
                            } else {
                                val months = ChronoUnit.DAYS.between(child!!.birthDate, it.date.toLocalDate()).toDouble() / 30.44
                                String.format(Locale.US, "%.1f міс", months)
                            }
                        } ?: "—"
                        val weightText = measurement?.weight?.let { String.format(Locale.US, "%.1f кг", it) } ?: "—"
                        val heightText = measurement?.height?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"
                        val bmiText = measurement?.let {
                            if (it.weight != null && it.height != null) {
                                String.format(Locale.US, "%.2f", it.weight!! / (it.height!! / 100.0).pow(2.0))
                            } else {
                                "—"
                            }
                        } ?: "—"
                        val headCircText = measurement?.headCirc?.let { String.format(Locale.US, "%.1f см", it) } ?: "—"

                        DetailRow(label = "Дата", value = formattedDate)
                        DetailRow(label = "Вік", value = ageMonths)
                        DetailRow(label = "Вага", value = weightText)
                        DetailRow(label = "Зріст", value = heightText)
                        DetailRow(label = "ІМТ", value = bmiText)
                        DetailRow(label = "Обхват голови", value = headCircText)
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = lastReport != null && (lastReport.text.isNotBlank() || lastReport.recommendations.isNotBlank()),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
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

        if (measurements.isEmpty()) {
            item {
                EmptyMeasurementsState()
            }
        } else {
            items(measurements) { measurement ->
                MeasurementItem(measurement)
            }
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

private fun Double.roundToChartPrecision(): Double {
    return "%.4f".format(Locale.US, this).toDouble()
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = CurakidTheme.typography.bodyMedium,
            color = CurakidTheme.colors.onSurfaceVariant
        )
        Text(
            text = value,
            style = CurakidTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = CurakidTheme.colors.onSurface
        )
    }
}

@Composable
private fun TopSummaryRow(
    child: Child?,
    measurements: List<Measurement>,
    norms: List<ChartNormPoint>,
    selectedChartType: GrowthChartType
) {
    val last = measurements.lastOrNull()

    // Helper to get value for selected chart type
    fun measurementValue(m: Measurement): Double? = when (selectedChartType) {
        GrowthChartType.WeightForAge -> m.weight
        GrowthChartType.HeightForAge -> m.height
        GrowthChartType.WeightForHeight -> m.weight
        GrowthChartType.BMI -> if (m.weight != null && m.height != null) {
            val w = m.weight!!
            val h = m.height!!
            w / (h / 100.0).pow(2.0)
        } else null

        GrowthChartType.HeadCircumference -> m.headCirc
    }

    // Estimate percentile and z-score by nearest norm
    val percentileText: String
    val zScoreText: String
    if (child == null || last == null || norms.isEmpty()) {
        percentileText = "—"
        zScoreText = "—"
    } else {
        val x = if (selectedChartType == GrowthChartType.WeightForHeight) {
            last.height ?: 0.0
        } else {
            ChronoUnit.DAYS.between(child.birthDate, last.date.toLocalDate()).toDouble() / 30.44
        }

        val value = measurementValue(last)
        if (value == null) {
            percentileText = "—"
            zScoreText = "—"
        } else {
            // find nearest norm point
            val nearest =
                norms.minByOrNull { kotlin.math.abs(it.x / (if (selectedChartType == GrowthChartType.WeightForHeight) 1.0 else 30.44) - x) }
            if (nearest == null) {
                percentileText = "—"
                zScoreText = "—"
            } else {
                // Simple interpolation between stored percentiles
                fun interp(pLow: Double, pHigh: Double, valLow: Double, valHigh: Double): Double {
                    if (valHigh == valLow) return (pLow + pHigh) / 2.0
                    val t = ((value - valLow) / (valHigh - valLow)).coerceIn(0.0, 1.0)
                    return pLow + t * (pHigh - pLow)
                }

                val pct = when {
                    value <= nearest.p3 -> 3.0
                    value <= nearest.p15 -> interp(3.0, 15.0, nearest.p3, nearest.p15)
                    value <= nearest.p50 -> interp(15.0, 50.0, nearest.p15, nearest.p50)
                    value <= nearest.p85 -> interp(50.0, 85.0, nearest.p50, nearest.p85)
                    value <= nearest.p97 -> interp(85.0, 97.0, nearest.p85, nearest.p97)
                    else -> 97.0
                }

                percentileText = "${pct.toInt()}‑й"

                // Map percentile to z approximately using anchors: p3:-1.88, p15:-1.04, p50:0, p85:1.04, p97:1.88
                val z = when {
                    pct <= 3 -> -1.88
                    pct <= 15 -> interp(-1.88, -1.04, 3.0, 15.0)
                    pct <= 50 -> interp(-1.04, 0.0, 15.0, 50.0)
                    pct <= 85 -> interp(0.0, 1.04, 50.0, 85.0)
                    pct <= 97 -> interp(1.04, 1.88, 85.0, 97.0)
                    else -> 1.88
                }

                zScoreText = String.format(Locale.US, "%.2f", z)
            }
        }
    }

    // Trend: compare last two values
    val trendText: String
    val trendIconColor: Color
    run {
        val lastVal = last?.let { measurementValue(it) }
        val prev = measurements.dropLast(1).lastOrNull()
        val prevVal = prev?.let { measurementValue(it) }
        if (lastVal == null || prevVal == null) {
            trendText = "—"
            trendIconColor = Color.Gray
        } else {
            val delta = lastVal - prevVal
            when {
                kotlin.math.abs(delta) < (kotlin.math.abs(prevVal) * 0.02 + 0.01) -> {
                    trendText = "Стабільно"
                    trendIconColor = Color.Gray
                }

                delta > 0 -> {
                    trendText = "Зростає"
                    trendIconColor = Color(0xFF4CAF50)
                }

                else -> {
                    trendText = "Знижується"
                    trendIconColor = Color(0xFFEF5350)
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Останній Z‑score",
            value = zScoreText,
            subtitle = "Статус",
            tint = Color(0xFF4CAF50)
        )
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Перцентиль",
            value = percentileText,
            subtitle = "з 100 дітей",
            tint = CurakidTheme.colors.primary
        )
        TopStatCard(
            modifier = Modifier.weight(1f),
            title = "Тренд",
            value = trendText,
            subtitle = "Порівняно",
            tint = trendIconColor
        )
    }
}


@Composable
private fun TopStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    tint: Color
) {
    Card(
        modifier = modifier
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = CurakidTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = CurakidTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tint,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = CurakidTheme.typography.labelSmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

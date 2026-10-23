@file:OptIn(ExperimentalMaterial3Api::class)

package com.misterio.curakid.feature.development

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import com.misterio.core.domain.model.Measurement
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
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
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GrowthChartCard(
    chartData: List<Measurement>,
    selectedChartType: GrowthChartType,
    chartReady: Boolean,
    chartRangeY: Pair<Double, Double>?,
    hasNormsDrawn: Boolean,
    modelProducer: CartesianChartModelProducer,
    markerVisibilityListener: CartesianMarkerVisibilityListener,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_data_yet), color = Color.Gray)
        }
        return
    }

    val styles = rememberGrowthChartStyles()
    val legend = rememberGrowthChartLegend(
        mainLineColor = CurakidTheme.colors.primary,
        warningLineColor = CuraKidColors.Amber30,
        criticalLineColor = CurakidTheme.colors.error
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

    BoxWithConstraints(
        modifier = modifier
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
            colors = CardDefaults.cardColors(containerColor = CurakidTheme.colors.primaryContainer),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(CurakidTheme.colors.surfaceVariant, CurakidTheme.colors.surface)
                        )
                    )
                    .padding(16.dp)
            ) {
                if (!chartReady || chartRangeY == null) {
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
                                            val childSeriesIndex = if (hasNormsDrawn) 5 else 0
                                            when {
                                                !hasNormsDrawn -> styles.childSeries
                                                seriesIndex == childSeriesIndex -> styles.childSeries
                                                seriesIndex == 0 -> styles.p3
                                                seriesIndex == 1 -> styles.p15
                                                seriesIndex == 2 -> styles.p50
                                                seriesIndex == 3 -> styles.p85
                                                seriesIndex == 4 -> styles.p97
                                                else -> styles.normLine
                                            }
                                        },
                                        rangeProvider = CartesianLayerRangeProvider.fixed(
                                            minY = chartRangeY.first,
                                            maxY = chartRangeY.second
                                        )
                                    ),
                                    getXStep = { _, _, _ -> 1.0 },
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
                                                if (selectedChartType == GrowthChartType.WeightForHeight) 5 else 4
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

@Composable
fun EmptyMeasurementsState(
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
            text = stringResource(R.string.empty_measurements_history),
            style = CurakidTheme.typography.bodyLarge,
            color = CurakidTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

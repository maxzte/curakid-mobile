package com.misterio.curakid.feature.development

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.misterio.core.ui.theme.CuraKidColors
import com.misterio.core.ui.theme.CurakidTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.HorizontalLegend
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LegendItem
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend

internal class GrowthChartStyles(
    val mainLine: LineCartesianLayer.Line,
    val normLine: LineCartesianLayer.Line,
    val p3: LineCartesianLayer.Line,
    val p15: LineCartesianLayer.Line,
    val p50: LineCartesianLayer.Line,
    val p85: LineCartesianLayer.Line,
    val p97: LineCartesianLayer.Line,
    val childSeries: LineCartesianLayer.Line
)

@Composable
internal fun rememberGrowthChartStyles(): GrowthChartStyles {
    val mainLineColor = CurakidTheme.colors.primary
    val normLineColor = Color.LightGray.copy(alpha = 0.5f)
    val warningLineColor = CuraKidColors.Amber30
    val criticalLineColor = CurakidTheme.colors.error

    val mainLineStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(mainLineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val normLineStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(normLineColor)),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p3Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(criticalLineColor.copy(alpha = 0.5f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p15Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.5f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p50Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(Color.LightGray.copy(alpha = 0.8f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.5.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p85Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(warningLineColor.copy(alpha = 0.5f))),
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 1.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    val p97Style = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(criticalLineColor.copy(alpha = 0.5f))),
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
            LineCartesianLayer.Point(childPointComponent, size = 8.dp)
        )
    }

    val childSeriesStyle = LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(Fill(mainLineColor)),
        pointProvider = childPointProvider,
        stroke = LineCartesianLayer.LineStroke.Continuous(thickness = 3.dp),
        interpolator = LineCartesianLayer.Interpolator.cubic()
    )

    return remember(
        mainLineStyle,
        normLineStyle,
        p3Style,
        p15Style,
        p50Style,
        p85Style,
        p97Style,
        childSeriesStyle
    ) {
        GrowthChartStyles(
            mainLineStyle,
            normLineStyle,
            p3Style,
            p15Style,
            p50Style,
            p85Style,
            p97Style,
            childSeriesStyle
        )
    }
}

@Composable
internal fun rememberGrowthChartLegend(
    mainLineColor: Color,
    warningLineColor: Color,
    criticalLineColor: Color
): HorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext> {
    val labelComponent = rememberTextComponent(
        style = CurakidTheme.typography.labelSmall.copy(color = Color.Gray)
    )
    val mainIcon = rememberShapeComponent(fill = Fill(mainLineColor), shape = CircleShape)
    val normIcon = rememberShapeComponent(fill = Fill(CurakidTheme.colors.tertiaryContainer), shape = CircleShape)
    val warnIcon = rememberShapeComponent(
        fill = Fill(warningLineColor.copy(alpha = 0.9f)),
        shape = CircleShape
    )
    val critIcon = rememberShapeComponent(
        fill = Fill(criticalLineColor.copy(alpha = 0.9f)),
        shape = CircleShape
    )

    val normLabel = androidx.compose.ui.res.stringResource(R.string.legend_norm)
    val warnLabel = androidx.compose.ui.res.stringResource(R.string.legend_attention)
    val critLabel = androidx.compose.ui.res.stringResource(R.string.legend_critical)
    val childLabel = androidx.compose.ui.res.stringResource(R.string.legend_child)

    return rememberHorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
        items = {
            add(
                LegendItem(
                    icon = normIcon,
                    labelComponent = labelComponent,
                    label = normLabel
                )
            )
            add(
                LegendItem(
                    icon = warnIcon,
                    labelComponent = labelComponent,
                    label = warnLabel
                )
            )
            add(
                LegendItem(
                    icon = critIcon,
                    labelComponent = labelComponent,
                    label = critLabel
                )
            )
            add(LegendItem(icon = mainIcon, labelComponent = labelComponent, label = childLabel))
        },
        padding = Insets(top = 8.dp),
    )
}

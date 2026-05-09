package com.misterio.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun RulerSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    step: Float = 0.1f,
    indicatorColor: Color = Color(0xFF6200EE)
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val lineSpacing = with(density) { 10.dp.toPx() }
    val majorLineHeight = with(density) { 30.dp.toPx() }
    val minorLineHeight = with(density) { 10.dp.toPx() }

    val scope = rememberCoroutineScope()
    val animatableValue = remember { Animatable(value) }

    fun snapToTick(value: Float, step: Float): Float {
        return (round(value / step) * step)
    }

    // Sync animatableValue with external value if external change is not from animation
    LaunchedEffect(value) {
        if (!animatableValue.isRunning) {
            animatableValue.snapTo(value)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    val deltaValue = (delta / lineSpacing) * step
                    val newValue = (animatableValue.value - deltaValue).coerceIn(range)
                    scope.launch {
                        animatableValue.snapTo(newValue)
                    }
                    onValueChange(newValue)
                },
                onDragStopped = { velocity ->
                    // Convert velocity (px/s) to value change rate
                    val velocityInValue = (velocity / lineSpacing) * step
                    scope.launch {
                        try {
                            animatableValue.animateDecay(
                                initialVelocity = -velocityInValue,
                                animationSpec = androidx.compose.animation.core.exponentialDecay(
                                    frictionMultiplier = 1.5f
                                )
                            ) {
                                val currentValue = this.value
                                if (currentValue < range.start || currentValue > range.endInclusive) {
                                    val clamped = currentValue.coerceIn(range)
                                    onValueChange(clamped)
                                    // Use a non-suspending way to trigger end of animation or handle it after
                                    throw IllegalStateException("Boundary reached")
                                } else {
                                    onValueChange(currentValue)
                                }
                            }
                        } catch (e: Exception) {
                            // If we hit the boundary or exception, snapping will happen below
                        } finally {
                            // Ensure we snap exactly to a tick after decay or drag
                            val finalValue = snapToTick(animatableValue.value, step).coerceIn(range)
                            animatableValue.animateTo(finalValue)
                            onValueChange(finalValue)
                        }
                    }
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            val center = size.width / 2
            val currentValue = animatableValue.value
            val startValue = (currentValue - (center / lineSpacing) * step)
            val endValue = (currentValue + (center / lineSpacing) * step)
            
            val firstVisibleTick = (startValue / step).roundToInt()
            val lastVisibleTick = (endValue / step).roundToInt()

            for (tick in firstVisibleTick..lastVisibleTick) {
                val tickValue = tick * step
                if (tickValue !in range.start - step..range.endInclusive + step) continue
                
                val x = center + (tickValue - currentValue) / step * lineSpacing
                val isMajor = tick % 10 == 0
                val lineHeight = if (isMajor) majorLineHeight else minorLineHeight
                val color = if (tickValue in range) Color.LightGray else Color.Transparent

                drawLine(
                    color = color,
                    start = Offset(x, size.height / 2 - lineHeight / 2),
                    end = Offset(x, size.height / 2 + lineHeight / 2),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )

                if (isMajor && tickValue in range) {
                    val text = tickValue.toInt().toString()
                    val textResult = textMeasurer.measure(text, TextStyle(fontSize = 12.sp, color = Color.Gray))
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(x - textResult.size.width / 2, size.height / 2 + lineHeight / 2 + 4.dp.toPx())
                    )
                }
            }

            // Draw center indicator
            val path = Path().apply {
                moveTo(center, size.height / 2 + majorLineHeight / 2 + 10.dp.toPx())
                lineTo(center - 8.dp.toPx(), size.height / 2 + majorLineHeight / 2 + 20.dp.toPx())
                lineTo(center + 8.dp.toPx(), size.height / 2 + majorLineHeight / 2 + 20.dp.toPx())
                close()
            }
            drawPath(path, indicatorColor)
            drawLine(
                color = indicatorColor,
                start = Offset(center, size.height / 2 - majorLineHeight / 2),
                end = Offset(center, size.height / 2 + majorLineHeight / 2),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

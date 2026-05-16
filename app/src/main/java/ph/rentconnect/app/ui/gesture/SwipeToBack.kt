package ph.rentconnect.app.ui.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.launch

fun Modifier.swipeToBack(
    onDismiss: () -> Unit,
    startZoneWidth: Dp = 80.dp,
    edgeOnlyAboveY: Dp? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val startZonePx = with(density) { startZoneWidth.toPx() }
    val edgeOnlyAboveYPx = edgeOnlyAboveY?.let { with(density) { it.toPx() } }
    val minDistancePx = with(density) { 12.dp.toPx() }
    val thresholdPx = with(density) { 80.dp.toPx() }
    val velocityThresholdPx = with(density) { 200.dp.toPx() }
    val narrowEdgePx = with(density) { 40.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    this
        .graphicsLayer { translationX = offsetX.value }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent()
                    if (down.type != PointerEventType.Press) continue
                    val firstChange = down.changes.firstOrNull() ?: continue

                    val startX = firstChange.position.x
                    val startY = firstChange.position.y

                    val effectiveZone = if (edgeOnlyAboveYPx != null && startY < edgeOnlyAboveYPx) {
                        narrowEdgePx
                    } else {
                        startZonePx
                    }
                    if (startX > effectiveZone) continue

                    var totalDx = 0f
                    var totalDy = 0f
                    var tracking = false
                    val velocityTracker = VelocityTracker()

                    var gestureEnded = false
                    while (!gestureEnded) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()

                        if (!change.pressed) {
                            // Finger lifted
                            val velocity = velocityTracker.calculateVelocity().x
                            val predictedEnd = totalDx + velocity * 0.1f
                            scope.launch {
                                if (totalDx > thresholdPx || predictedEnd > velocityThresholdPx) {
                                    offsetX.animateTo(screenWidthPx, tween(180, easing = EaseOut))
                                    onDismiss()
                                } else {
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = 400f))
                                }
                            }
                            gestureEnded = true
                            break
                        }

                        // Child already handled this event (TextField, Pager, Map, etc.)
                        if (change.isConsumed) {
                            gestureEnded = true
                            break
                        }

                        val drag = change.positionChange()
                        totalDx += drag.x
                        totalDy += drag.y
                        velocityTracker.addPosition(change.uptimeMillis, change.position)

                        if (!tracking) {
                            if (abs(totalDx) > minDistancePx && totalDx > 0 && abs(totalDx) > abs(totalDy)) {
                                tracking = true
                            } else if (abs(totalDy) > minDistancePx) {
                                // Vertical scroll — abandon gesture
                                gestureEnded = true
                                break
                            } else {
                                continue
                            }
                        }

                        if (tracking) {
                            scope.launch { offsetX.snapTo(max(0f, totalDx)) }
                            change.consume()
                        }
                    }
                }
            }
        }
}

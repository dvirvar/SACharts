package com.skellyapps.charts.common.extension

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs

suspend fun PointerInputScope.detectTransformGestures(
    consume: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Initial,
    onGesture: (
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        type: PointerEventType,
        changes: List<PointerInputChange>
    ) -> Unit,
) {
    awaitEachGesture {
        // Wait for at least one pointer to press down, and set first contact position
        val pointerEvent = awaitPointerEvent(pass)
        if (pointerEvent.type == PointerEventType.Scroll) {
            val firstChange = pointerEvent.changes.first()
            onGesture(
                firstChange.position,
                pointerEvent.calculatePan(),
                firstChange.scrollDelta.y,
                pointerEvent.type,
                pointerEvent.changes
            )
        } else if (pointerEvent.type == PointerEventType.Press) {
            var zoom = 1f
            var pastTouchSlop = false
            val touchSlop = viewConfiguration.touchSlop
            do {
                val event = awaitPointerEvent(pass = pass)
                // If any position change is consumed from another PointerInputChange
                // or pointer count requirement is not fulfilled
                val canceled = event.changes.fastAny { it.isConsumed }
                if (canceled) continue
                if (event.type == PointerEventType.Scroll) {
                    val firstChange = event.changes.first()
                    onGesture(
                        firstChange.position,
                        event.calculatePan(),
                        firstChange.scrollDelta.y,
                        event.type,
                        event.changes
                    )
                    continue
                }

                val zoomChange = event.calculateZoom()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize

                    if (zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f) {
                        onGesture(
                            centroid,
                            panChange,
                            zoomChange,
                            pointerEvent.type,
                            event.changes
                        )
                    }

                    if (consume) {
                        event.changes.fastForEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
        }
    }
}
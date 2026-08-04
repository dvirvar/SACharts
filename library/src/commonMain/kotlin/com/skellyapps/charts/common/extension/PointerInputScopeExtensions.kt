package com.skellyapps.charts.common.extension

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs

internal suspend fun PointerInputScope.detectTransformGestures(
    consume: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Initial,
    onGesture: (
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        orientation: Orientation?,
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
                null,
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
                        null,
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
                    val zoomMotion = abs(1f - zoom) * centroidSize

                    if (zoomMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    if (zoomChange != 1f) {
                        val centroid = event.calculateCentroid(useCurrent = false)
                        var totalHorizontalMovement = 0f
                        var totalVerticalMovement = 0f
                        event.changes.fastForEach { change ->
                            val previousPosition = change.previousPosition
                            val currentPosition = change.position

                            val dx = currentPosition.x - previousPosition.x
                            val dy = currentPosition.y - previousPosition.y

                            totalHorizontalMovement += abs(dx)
                            totalVerticalMovement += abs(dy)
                        }
                        val orientation = if (totalHorizontalMovement > totalVerticalMovement * 2f) { // e.g., 1.5f
                            // Zoom is predominantly horizontal
                            // You might want to pass this information in your onGesture or
                            // have a separate callback for it.
                            Orientation.Horizontal
                        } else if (totalVerticalMovement > totalHorizontalMovement * 2f) {
                            // Zoom is predominantly vertical
                            Orientation.Vertical
                        } else {
                            // More or less equal, or too small to determine
                             null
                        }
                        onGesture(
                            centroid,
                            panChange,
                            zoomChange,
                            orientation,
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

internal suspend fun PointerInputScope.detectDragGesturesUnconsumed(
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var drag: PointerInputChange?
        var overSlop: Offset = Offset.Zero

        do {
            drag =
                awaitPointerSlopOrCancellation(down.id, down.type) {
                        change,
                        over ->
                    change.consume()
                    overSlop = over
                }
        } while (drag != null && !drag.isConsumed)


        // if the pointer is still down, keep reading events in case we need to pick up the gesture.
        while (drag == null && currentEvent.changes.fastAny { it.pressed }) {
            var event: PointerEvent
            do {
                // use final pass so we only pick up a gesture if it was really ignored by
                // everyone else
                event = awaitPointerEvent(pass = PointerEventPass.Final)
            } while (event.changes.fastAny { it.isConsumed } && event.changes.fastAny { it.pressed })

            // an event was not consumed and there's still a pointer in the screen
            if (event.changes.fastAny { it.pressed }) {
                // await touch slop again, using the initial down as starting point.
                // For most cases this should return immediately since we probably moved
                // far enough from the initial down event.
                val initialPositionChange =
                    (event.changes.firstOrNull()?.position ?: Offset.Zero) - down.position
                drag =
                    awaitPointerSlopOrCancellation(
                        down.id,
                        down.type,
                        initialPositionChange = initialPositionChange,
                    ) { change, _ ->
                        // the triggering event will be used as over slop
                        overSlop = change.positionChange()
                    }
            }
        }

        if (drag != null) {
            onDragStart(drag.position)
            onDrag(drag, overSlop)
            val upEvent =
                drag(
                    pointerId = drag.id,
                    onDrag = {
                        onDrag(it, it.positionChange())
                    },
                    // once drag starts we want to capture drags in any direction, though
                    // they will be propagated on the correct direction above we want to
                    // consume any new drag to avoid the cases where we start dragging
                    // on a given direction and then change directions.
                    motionConsumed = { it.isConsumed },
                )
            if (upEvent == null) {
                onDragCancel()
            } else {
                onDragEnd()
            }
        }
    }
}
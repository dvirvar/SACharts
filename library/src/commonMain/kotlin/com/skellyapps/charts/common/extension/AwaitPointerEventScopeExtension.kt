package com.skellyapps.charts.common.extension

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull

internal suspend inline fun AwaitPointerEventScope.awaitPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    initialPositionChange: Offset = Offset.Zero,
    onPointerSlopReached: (PointerInputChange, Offset) -> Unit,
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }

    val touchSlop = viewConfiguration.pointerSlop(pointerType)
    var pointer: PointerId = pointerId
    val touchSlopDetector = TouchSlopDetector(initialPositionChange)
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.isConsumed) {
            return null
        } else if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return null
            } else {
                pointer = otherDown.id
            }
        } else {
            val postSlopOffset =
                touchSlopDetector.getPostSlopOffset(
                    dragEvent.positionChangeIgnoreConsumed(),
                    touchSlop,
                )
            if (postSlopOffset.isSpecified) {
                onPointerSlopReached(dragEvent, postSlopOffset)
                if (dragEvent.isConsumed) {
                    return dragEvent
                } else {
                    touchSlopDetector.reset()
                }
            } else {
                // verify that nothing else consumed the drag event
                awaitPointerEvent(PointerEventPass.Final)
                if (dragEvent.isConsumed) {
                    return null
                }
            }
        }
    }
}

private fun PointerEvent.isPointerUp(pointerId: PointerId): Boolean =
    changes.fastFirstOrNull { it.id == pointerId }?.pressed != true

private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop

private fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
    return when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }
}

internal class TouchSlopDetector(
    initialPositionChange: Offset = Offset.Zero,
) {

    fun Offset.mainAxis() = y

    fun Offset.crossAxis() = x

    /** The accumulation of drag deltas in this detector. */
    private var totalPositionChange: Offset = initialPositionChange

    /**
     * Adds [dragEvent] to this detector. If the accumulated position changes crosses the touch slop
     * provided by [touchSlop], this method will return the post slop offset, that is the total
     * accumulated delta change minus the touch slop value, otherwise this should return null. If
     * [shouldCommit] is true, the delta will be added to the total position change.
     */
    fun getPostSlopOffset(
        positionChange: Offset,
        touchSlop: Float,
        shouldCommit: Boolean = true,
    ): Offset {
        val finalChange =
            if (shouldCommit) {
                totalPositionChange += positionChange
                totalPositionChange
            } else {
                totalPositionChange + positionChange
            }

        val inDirection = finalChange.getDistance()
        val hasCrossedSlop = inDirection >= touchSlop

        return if (hasCrossedSlop) {
            calculatePostSlopOffset(touchSlop)
        } else {
            Offset.Unspecified
        }
    }

    /**
     * Resets the accumulator associated with this detector.
     *
     * @param initialPositionAccumulator Use to initialize the position change accumulator, for
     *   instance in cases where slop detection may happen "mid-gesture", that is, the slop
     *   detection didn't start from the first down event but somewhere after.
     */
    fun reset(initialPositionAccumulator: Offset = Offset.Zero) {
        totalPositionChange = initialPositionAccumulator
    }

    private fun calculatePostSlopOffset(touchSlop: Float): Offset {
        val touchSlopOffset =
            totalPositionChange / totalPositionChange.getDistance() * touchSlop
        // update postSlopOffset
        return totalPositionChange - touchSlopOffset
    }
}

internal suspend inline fun AwaitPointerEventScope.drag(
    pointerId: PointerId,
    onDrag: (PointerInputChange) -> Unit,
    motionConsumed: (PointerInputChange) -> Boolean,
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }
    var pointer = pointerId
    while (true) {
        val change =
            awaitDragOrUp(pointer) {
                val positionChange = it.positionChangeIgnoreConsumed()
                val motionChange = positionChange.getDistance()
                motionChange != 0.0f
            } ?: return null

        if (motionConsumed(change)) {
            return null
        }

        if (change.changedToUpIgnoreConsumed()) {
            return change
        }

        onDrag(change)
        pointer = change.id
    }
}

private suspend inline fun AwaitPointerEventScope.awaitDragOrUp(
    pointerId: PointerId,
    hasDragged: (PointerInputChange) -> Boolean,
): PointerInputChange? {
    var pointer = pointerId
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return dragEvent
            } else {
                pointer = otherDown.id
            }
        } else if (hasDragged(dragEvent)) {
            return dragEvent
        }
    }
}
package com.skellyapps.charts.common.model

import kotlin.jvm.JvmInline

@JvmInline
value class Position private constructor(val mask: Int) {
    companion object {
        val Top    = Position(1 shl 0)
        val Bottom = Position(1 shl 1)

        val Left   = Position(1 shl 2)
        val Right  = Position(1 shl 3)

        val TopLeft      = Top or Left
        val TopRight     = Top or Right
        val Center       = Position(0)
        val BottomLeft   = Bottom or Left
        val BottomRight  = Bottom or Right
    }

    infix fun or(other: Position): Position = Position(mask or other.mask)
    infix fun and(other: Position): Position = Position(mask and other.mask)
    operator fun contains(other: Position): Boolean = (mask and other.mask) == other.mask
}
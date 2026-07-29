package com.skellyapps.charts.bar.extension

import androidx.compose.ui.geometry.Offset
import com.skellyapps.charts.bar.model.BarChartData
import com.skellyapps.charts.bar.model.BarChartData.OffsetCategory
import com.skellyapps.charts.bar.model.TaggedOffset
import kotlin.jvm.JvmInline

internal fun List<OffsetCategory>.binarySearch(offset: Offset, type: BarChartData.Type, isXDominant: Boolean): TaggedOffset? {
    return binarySearch(offset, type, isXDominant, 0, 0)
}

private fun List<OffsetCategory>.binarySearch(offset: Offset, type: BarChartData.Type, isXDominant: Boolean, categoryIndex: Int, fromOffsetIndex: Int): TaggedOffset? {
    if (isEmpty()) {
        return null
    }

    when (val result = this[categoryIndex].binarySearch(offset, isXDominant, fromOffsetIndex)) {
        is BinarySearchResult.Found -> return TaggedOffset(result.offset, this[categoryIndex].tag, result.index)
        is BinarySearchResult.InXButNotY-> {
            if (type is BarChartData.Type.Stacked) {
                for (i in (categoryIndex + 1)..this.lastIndex) {
                    val o = this[i].offsets.getOrNull(result.index) ?: continue
                    if (o.containsInYDimension(offset.y)) {
                        return TaggedOffset(o, this[i].tag, result.index)
                    }
                }
            }
            return null
        }
        is BinarySearchResult.InYButNotX -> {
            if (type is BarChartData.Type.Stacked) {
                for (i in (categoryIndex + 1)..this.lastIndex) {
                    val o = this[i].offsets.getOrNull(result.index) ?: continue
                    if (o.containsInXDimension(offset.x)) {
                        return TaggedOffset(o, this[i].tag, result.index)
                    }
                }
            }
            return null
        }
        is BinarySearchResult.InsertionPoint -> {
            if (result.index == this[categoryIndex].offsets.size) {
                for (i in (categoryIndex + 1)..this.lastIndex) {
                    if (result.index - 1 <= this[i].offsets.lastIndex) {
                        return binarySearch(offset, type, isXDominant,i, result.index - 1)
                    }
                }
            } else {
                for (i in (categoryIndex + 1)..this.lastIndex) {
                    val o = this[i].offsets.getOrNull(result.index - 1) ?: continue
                    if (o.contains(offset)) {
                        return TaggedOffset(o, this[i].tag, result.index - 1)
                    }
                }
            }
            return null
        }
        BinarySearchResult.Empty -> {
            val nextCategoryIndex = categoryIndex + 1
            if (nextCategoryIndex > this.lastIndex) {
                return null
            }
            return binarySearch(offset, type, isXDominant, nextCategoryIndex, fromOffsetIndex)
        }
    }
}

private fun OffsetCategory.binarySearch(offset: Offset,isXDominant: Boolean, fromIndex: Int = 0, toIndex: Int = offsets.lastIndex): BinarySearchResult {
    if (offsets.isEmpty()) {
        return BinarySearchResult.Empty
    }
    var low = fromIndex
    var high = toIndex

    while (low <= high) {
        val mid = (low + high).ushr(1)
        val midVal = offsets[mid]

        when(isXDominant) {
            true -> {
                when {
                    offset.x < midVal.topLeft.x.value -> high = mid - 1 //Lower than x mid-point
                    offset.x > midVal.topLeft.x.value + midVal.size.width -> low = mid + 1 //higher than x mid-point
                    else -> {//Inside x
                        if (midVal.containsInYDimension(offset.y)) {
                            return BinarySearchResult.Found(midVal, mid)
                        }
                        return BinarySearchResult.InXButNotY(mid)
                    }
                }
            }
            false -> {
                when {
                    offset.y < midVal.topLeft.y.value -> high = mid - 1 //Lower than x mid-point
                    offset.y > midVal.topLeft.y.value + midVal.size.height -> low = mid + 1 //higher than x mid-point
                    else -> {//Inside y
                        if (midVal.containsInXDimension(offset.x)) {
                            return BinarySearchResult.Found(midVal, mid)
                        }
                        return BinarySearchResult.InYButNotX(mid)
                    }
                }
            }
        }
    }
    return BinarySearchResult.InsertionPoint(low)
}

internal fun OffsetCategory.Offset.containsInXDimension(x: Float): Boolean {
    val value = topLeft.x.value
    return x >= value && x <= value + size.width
}

internal fun OffsetCategory.Offset.containsInYDimension(y: Float): Boolean {
    val value = topLeft.y.value
    return y >= value && y <= value + size.height
}

internal fun OffsetCategory.Offset.contains(offset: Offset): Boolean {
    return containsInXDimension(offset.x) && containsInYDimension(offset.y)
}

private sealed interface BinarySearchResult {
    data class Found(val offset: OffsetCategory.Offset, val index: Int): BinarySearchResult
    @JvmInline
    value class InsertionPoint(val index: Int): BinarySearchResult
    @JvmInline
    value class InXButNotY(val index: Int): BinarySearchResult
    @JvmInline
    value class InYButNotX(val index: Int): BinarySearchResult
    data object Empty: BinarySearchResult
}
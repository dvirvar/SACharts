package com.skellyapps.charts.bar.model

import androidx.compose.runtime.SnapshotMutationPolicy

internal data class TaggedOffset(
    val offset: BarChartData.OffsetCategory.Offset,
    val categoryTag: Int,
    val index: Int
)

internal object TaggedOffsetEqualityPolicy : SnapshotMutationPolicy<TaggedOffset?> {
    override fun equivalent(a: TaggedOffset?, b: TaggedOffset?) = a?.offset === b?.offset

    override fun toString() = "TaggedOffsetEqualityPolicy"
}
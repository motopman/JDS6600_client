package com.jds6600.sequencer.presentation.builder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import com.jds6600.sequencer.domain.model.ToolboxItem

data class BlockBounds(val topY: Float, val bottomY: Float)

class DragAndDropState {
    var isDragging  by mutableStateOf(false)
    var dragItem    by mutableStateOf<ToolboxItem?>(null)
    var dragOffsetX by mutableFloatStateOf(0f)
    var dragOffsetY by mutableFloatStateOf(0f)
    var insertIndex by mutableIntStateOf(0)

    /** Bounds of each block row, in root-Box local coordinates. */
    val blockBounds = mutableStateMapOf<Int, BlockBounds>()

    /** Layout coordinates of the root Box – set via onGloballyPositioned. */
    var rootCoords: LayoutCoordinates? = null

    fun startDrag(item: ToolboxItem, rootSpaceOffset: Offset) {
        isDragging  = true
        dragItem    = item
        dragOffsetX = rootSpaceOffset.x
        dragOffsetY = rootSpaceOffset.y
        recalcInsertIndex()
    }

    fun updateDrag(delta: Offset) {
        if (!isDragging) return
        dragOffsetX += delta.x
        dragOffsetY += delta.y
        recalcInsertIndex()
    }

    /** Call on drag end; returns the resolved insert index before resetting. */
    fun endDrag(): Pair<ToolboxItem, Int>? {
        if (!isDragging || dragItem == null) {
            reset(); return null
        }
        val result = dragItem!! to insertIndex
        reset()
        return result
    }

    fun reset() {
        isDragging  = false
        dragItem    = null
        dragOffsetX = 0f
        dragOffsetY = 0f
        insertIndex = 0
        blockBounds.clear()
    }

    fun updateBlockBounds(index: Int, topY: Float, bottomY: Float) {
        blockBounds[index] = BlockBounds(topY, bottomY)
    }

    private fun recalcInsertIndex() {
        val dragY = dragOffsetY
        val sorted = blockBounds.entries.sortedBy { it.key }
        var idx = sorted.size
        for ((i, bounds) in sorted) {
            val midY = (bounds.topY + bounds.bottomY) / 2f
            if (dragY < midY) { idx = i; break }
        }
        insertIndex = idx
    }
}

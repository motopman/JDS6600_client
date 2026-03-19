package com.jds6600.sequencer.presentation.builder

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jds6600.sequencer.domain.model.ToolboxItem
import com.jds6600.sequencer.utils.hueToColor

@Composable
fun ToolboxRow(
    items: List<ToolboxItem>,
    dragState: DragAndDropState,
    onDragEnd: (ToolboxItem, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ToolboxChip(item = item, dragState = dragState, onDragEnd = onDragEnd)
        }
    }
}

@Composable
private fun ToolboxChip(
    item: ToolboxItem,
    dragState: DragAndDropState,
    onDragEnd: (ToolboxItem, Int) -> Unit
) {
    var chipCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val color = hueToColor(item.hue)
    val isDraggingThis = dragState.isDragging && dragState.dragItem?.id == item.id

    Card(
        modifier = Modifier
            .size(width = 76.dp, height = 60.dp)
            .alpha(if (isDraggingThis) 0.4f else 1f)
            .onGloballyPositioned { chipCoords = it }
            .pointerInput(item.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        val root = dragState.rootCoords ?: return@detectDragGesturesAfterLongPress
                        val chip = chipCoords       ?: return@detectDragGesturesAfterLongPress
                        val rootOffset = root.localPositionOf(chip, localOffset)
                        dragState.startDrag(item, rootOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragState.updateDrag(dragAmount)
                    },
                    onDragEnd = {
                        val result = dragState.endDrag()
                        if (result != null) onDragEnd(result.first, result.second)
                    },
                    onDragCancel = { dragState.reset() }
                )
            },
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ChipContent(chakraName = item.chakraName, chakraShort = item.chakraShort, color = color)
    }
}

@Composable
fun ChipContent(chakraName: String, chakraShort: String, color: Color) {
    Column(
        modifier = Modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                drawCircle(color = color)
            }
        }
        Text(
            chakraName,
            fontSize = 8.sp,
            maxLines = 1,
            color = color
        )
        Text(
            chakraShort,
            fontSize = 8.sp,
            maxLines = 1,
            color = color.copy(alpha = 0.7f)
        )
    }
}

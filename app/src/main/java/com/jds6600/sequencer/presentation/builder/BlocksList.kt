package com.jds6600.sequencer.presentation.builder

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.presentation.components.BlockCard

@Composable
fun BlocksList(
    blocks: List<SequenceBlock>,
    dragState: DragAndDropState,
    onBlockClick: (SequenceBlock) -> Unit,
    onDeleteBlock: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.padding(horizontal = 12.dp)) {
        itemsIndexed(blocks, key = { _, b -> b.id }) { index, block ->
            // Insertion indicator above this block
            if (dragState.isDragging && dragState.insertIndex == index) {
                InsertionIndicator()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        val root = dragState.rootCoords
                        if (root != null) {
                            val topLeft = root.localPositionOf(coords,
                                androidx.compose.ui.geometry.Offset.Zero)
                            val botLeft = root.localPositionOf(coords,
                                androidx.compose.ui.geometry.Offset(
                                    0f, coords.size.height.toFloat()))
                            dragState.updateBlockBounds(index, topLeft.y, botLeft.y)
                        }
                    }
            ) {
                BlockCard(
                    block    = block,
                    onClick  = { onBlockClick(block) },
                    onDelete = { onDeleteBlock(block.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // Insertion indicator at the end
        item {
            if (dragState.isDragging && dragState.insertIndex == blocks.size) {
                InsertionIndicator()
            }
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun InsertionIndicator() {
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 4.dp),
        thickness = 2.dp,
        color     = MaterialTheme.colorScheme.primary
    )
}

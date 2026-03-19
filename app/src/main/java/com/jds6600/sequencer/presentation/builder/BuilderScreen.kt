package com.jds6600.sequencer.presentation.builder

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.domain.model.DEFAULT_TOOLBOX_ITEMS
import com.jds6600.sequencer.presentation.components.ConnectionStatusBadge
import com.jds6600.sequencer.utils.hueToColor
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    vm: BuilderViewModel = hiltViewModel()
) {
    val sequence      by vm.sequence.collectAsState()
    val connState      by vm.connectionState.collectAsState()
    val lastActivityMs by vm.lastActivityMs.collectAsState()
    val dragState     = remember { DragAndDropState() }
    val toolboxItems  = remember { DEFAULT_TOOLBOX_ITEMS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sequence.name) },
                actions = {
                    ConnectionStatusBadge(connState)
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            if (connState is ConnectionState.Connected) Icons.Default.Wifi
                            else Icons.Default.WifiOff,
                            contentDescription = "Connection"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned { dragState.rootCoords = it }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Toolbox ──────────────────────────────────────────────
                Text(
                    "Drag a chip onto the tray",
                    style   = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                ToolboxRow(
                    items     = toolboxItems,
                    dragState = dragState,
                    onDragEnd = { item, index -> vm.addBlockAt(item, index) }
                )
                HorizontalDivider()

                // ── Blocks tray ───────────────────────────────────────────
                BlocksList(
                    blocks       = sequence.blocks,
                    dragState    = dragState,
                    onBlockClick = { block ->
                        vm.startEditBlock(block)
                        onNavigateToEdit()
                    },
                    onDeleteBlock = vm::removeBlock,
                    modifier      = Modifier.weight(1f)
                )

                // ── Transport controls ────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${sequence.blocks.size} block(s)",
                            style = MaterialTheme.typography.labelSmall
                        )
                        if (lastActivityMs > 0L) {
                            val secsAgo = ((System.currentTimeMillis() - lastActivityMs) / 1000).coerceAtLeast(0)
                            Text(
                                "last contact: ${secsAgo}s ago",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (secsAgo < 90) androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                        else             androidx.compose.ui.graphics.Color(0xFFFF9800)
                            )
                        }
                    }
                    // Stop — always visible so user can halt mid-run
                    OutlinedButton(
                        onClick = vm::stopSequence,
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Stop")
                    }
                    // Send = upload sequence to server + start playback
                    Button(
                        onClick  = vm::playSequence,
                        enabled  = sequence.blocks.isNotEmpty() &&
                                   connState is ConnectionState.Connected
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Send")
                    }
                }
            }

            // ── Drag ghost overlay ────────────────────────────────────────
            if (dragState.isDragging) {
                val item = dragState.dragItem
                if (item != null) {
                    val color = hueToColor(item.hue)
                    Card(
                        shape   = RoundedCornerShape(10.dp),
                        colors  = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.85f)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .size(width = 76.dp, height = 60.dp)
                            .offset {
                                IntOffset(
                                    x = (dragState.dragOffsetX - 38.dp.toPx()).roundToInt(),
                                    y = (dragState.dragOffsetY - 30.dp.toPx()).roundToInt()
                                )
                            }
                            .alpha(0.85f)
                    ) {
                        ChipContent(chakraName = item.chakraName, chakraShort = item.chakraShort, color = color)
                    }
                }
            }
        }
    }
}

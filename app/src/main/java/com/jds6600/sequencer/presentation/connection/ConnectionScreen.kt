package com.jds6600.sequencer.presentation.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.presentation.components.ConnectionStatusBadge

@Composable
fun ConnectionScreen(
    onNavigateToQr: () -> Unit,
    onNavigateToBuilder: () -> Unit,
    vm: ConnectionViewModel
) {
    val host  by vm.host.collectAsState()
    val port  by vm.port.collectAsState()
    val token by vm.token.collectAsState()
    val state by vm.connectionState.collectAsState()

    // Auto-navigate when connected
    LaunchedEffect(state) {
        if (state is ConnectionState.Connected) onNavigateToBuilder()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("JDS6600 Sequencer", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Connect to the desktop service", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))

        ConnectionStatusBadge(state)
        Spacer(Modifier.height(24.dp))

        // QR Scan button
        Button(
            onClick = onNavigateToQr,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Spacer(Modifier.weight(1f))
            Text("Scan QR Code")
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        Text("— or enter manually —", style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = host,
            onValueChange = { vm.host.value = it },
            label = { Text("Host / IP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = port,
                onValueChange = { vm.port.value = it },
                label = { Text("Port") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = token,
                onValueChange = { vm.token.value = it },
                label = { Text("Token") },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = vm::connect,
            enabled = state !is ConnectionState.Connecting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state is ConnectionState.Connecting) "Connecting…" else "Connect")
        }

        if (state is ConnectionState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text  = (state as ConnectionState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToBuilder) {
            Text("Continue without service (offline)")
        }
    }
}

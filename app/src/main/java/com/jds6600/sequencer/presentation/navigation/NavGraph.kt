package com.jds6600.sequencer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jds6600.sequencer.presentation.connection.ConnectionScreen
import com.jds6600.sequencer.presentation.connection.ConnectionViewModel
import com.jds6600.sequencer.presentation.connection.QrScannerScreen
import com.jds6600.sequencer.presentation.builder.BuilderScreen
import com.jds6600.sequencer.presentation.edit.EditBlockScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Connection.route
    ) {
        composable(Screen.Connection.route) { connectionEntry ->
            // Single VM instance scoped to this back-stack entry
            val connectionVm: ConnectionViewModel = hiltViewModel(connectionEntry)
            ConnectionScreen(
                vm                  = connectionVm,
                onNavigateToQr      = { navController.navigate(Screen.QrScanner.route) },
                onNavigateToBuilder = {
                    navController.navigate(Screen.Builder.route) {
                        popUpTo(Screen.Connection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.QrScanner.route) { entry ->
            // Re-use the SAME VM instance that ConnectionScreen holds
            val connectionEntry = remember(entry) {
                navController.getBackStackEntry(Screen.Connection.route)
            }
            val connectionVm: ConnectionViewModel = hiltViewModel(connectionEntry)
            QrScannerScreen(
                vm           = connectionVm,
                onQrDetected = { navController.popBackStack() },
                onBack       = { navController.popBackStack() }
            )
        }

        composable(Screen.Builder.route) {
            BuilderScreen(
                onNavigateToEdit = { navController.navigate(Screen.EditBlock.route) },
                onNavigateBack   = { navController.navigate(Screen.Connection.route) }
            )
        }

        composable(Screen.EditBlock.route) {
            EditBlockScreen(
                onSaved  = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

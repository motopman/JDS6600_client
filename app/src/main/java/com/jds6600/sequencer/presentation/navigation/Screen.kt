package com.jds6600.sequencer.presentation.navigation

sealed class Screen(val route: String) {
    data object Connection : Screen("connection")
    data object QrScanner  : Screen("qr_scanner")
    data object Builder    : Screen("builder")
    data object EditBlock  : Screen("edit_block")
}

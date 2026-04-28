package com.jeiel.contextactionassistant.ui.home

data class HomeUiState(
    val overlayEnabled: Boolean = false,
    val overlayPermissionGranted: Boolean = false,
    val aiTransferEnabled: Boolean = true,
    val latestMessage: String = "대기 중",
    val isAnalyzing: Boolean = false
)

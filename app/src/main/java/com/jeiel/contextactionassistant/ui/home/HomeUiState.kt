package com.jeiel.contextactionassistant.ui.home

import com.jeiel.contextactionassistant.data.review.ReviewItem

data class HomeUiState(
    val overlayEnabled: Boolean = false,
    val overlayPermissionGranted: Boolean = false,
    val aiTransferEnabled: Boolean = true,
    val latestMessage: String = "대기 중",
    val isAnalyzing: Boolean = false,
    val reviewItems: List<ReviewItem> = emptyList(),
    val missingPermissions: List<String> = emptyList(),
    val reviewQuery: String = "",
    val reviewFilter: String = "ALL",
    val expandedReviewId: Long? = null
)

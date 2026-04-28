package com.jeiel.contextactionassistant.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ActionType {
    SCHEDULE,
    CODE,
    RECEIPT,
    NOTE,
    TODO,
    UNKNOWN
}

@Serializable
enum class CaptureSource {
    SCREENSHOT_DETECTED,
    MANUAL_CAPTURE,
    SHARED_IMAGE
}

@Serializable
data class ActionItem(
    val id: String,
    val label: String,
    val requiresConfirmation: Boolean
)

@Serializable
data class PrivacyFlags(
    val containsSensitiveData: Boolean = false,
    val sensitiveTypes: List<String> = emptyList()
)

@Serializable
data class AiAnalysisResult(
    val type: ActionType,
    val confidence: Double,
    val summary: String,
    val data: Map<String, String> = emptyMap(),
    val actions: List<ActionItem> = emptyList(),
    val privacyFlags: PrivacyFlags = PrivacyFlags()
)

data class AnalysisRequest(
    val imageBytes: ByteArray,
    val source: CaptureSource,
    val appPackage: String? = null,
    val screenMode: String = "PHONE",
    val userLocale: String = "ko-KR",
    val preferredActions: List<ActionType> = listOf(
        ActionType.SCHEDULE,
        ActionType.CODE,
        ActionType.RECEIPT,
        ActionType.NOTE,
        ActionType.TODO
    )
)

data class ProcessedImage(
    val bytes: ByteArray,
    val sha256: String
)

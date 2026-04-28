package com.jeiel.contextactionassistant.data.datastore

import kotlinx.coroutines.flow.Flow

data class UserSettings(
    val overlayEnabled: Boolean = false,
    val aiTransferEnabled: Boolean = true,
    val selectedCalendarId: Long? = null,
    val bubbleX: Int = 60,
    val bubbleY: Int = 300
)

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setOverlayEnabled(enabled: Boolean)
    suspend fun setAiTransferEnabled(enabled: Boolean)
    suspend fun setSelectedCalendarId(calendarId: Long?)
    suspend fun setBubblePosition(x: Int, y: Int)
}

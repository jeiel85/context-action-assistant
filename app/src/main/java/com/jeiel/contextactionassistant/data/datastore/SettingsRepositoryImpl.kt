package com.jeiel.contextactionassistant.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : SettingsRepository {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("context_action_settings") }
    )

    override val settings: Flow<UserSettings> = dataStore.data.map { pref ->
        UserSettings(
            overlayEnabled = pref[KEY_OVERLAY_ENABLED] ?: false,
            aiTransferEnabled = pref[KEY_AI_TRANSFER_ENABLED] ?: true,
            selectedCalendarId = pref[KEY_CALENDAR_ID],
            bubbleX = pref[KEY_BUBBLE_X] ?: 60,
            bubbleY = pref[KEY_BUBBLE_Y] ?: 300
        )
    }

    override suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_OVERLAY_ENABLED] = enabled }
    }

    override suspend fun setAiTransferEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AI_TRANSFER_ENABLED] = enabled }
    }

    override suspend fun setSelectedCalendarId(calendarId: Long?) {
        dataStore.edit {
            if (calendarId == null) {
                it.remove(KEY_CALENDAR_ID)
            } else {
                it[KEY_CALENDAR_ID] = calendarId
            }
        }
    }

    override suspend fun setBubblePosition(x: Int, y: Int) {
        dataStore.edit {
            it[KEY_BUBBLE_X] = x
            it[KEY_BUBBLE_Y] = y
        }
    }

    private companion object {
        val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val KEY_AI_TRANSFER_ENABLED = booleanPreferencesKey("ai_transfer_enabled")
        val KEY_CALENDAR_ID = longPreferencesKey("selected_calendar_id")
        val KEY_BUBBLE_X = intPreferencesKey("bubble_x")
        val KEY_BUBBLE_Y = intPreferencesKey("bubble_y")
    }
}

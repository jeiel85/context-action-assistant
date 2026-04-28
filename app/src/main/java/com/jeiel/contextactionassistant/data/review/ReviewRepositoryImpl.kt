package com.jeiel.contextactionassistant.data.review

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) : ReviewRepository {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("review_items") }
    )

    override suspend fun add(item: ReviewItem) {
        val current = getAll().toMutableList()
        current.add(0, item)
        val limited = current.take(MAX_ITEMS)
        dataStore.edit { pref ->
            pref[KEY_ITEMS] = json.encodeToString(limited)
        }
    }

    override suspend fun clear() {
        dataStore.edit { it[KEY_ITEMS] = "[]" }
    }

    override suspend fun getAll(): List<ReviewItem> {
        val raw = dataStore.data.first()[KEY_ITEMS] ?: "[]"
        return runCatching {
            json.decodeFromString<List<ReviewItem>>(raw)
        }.getOrDefault(emptyList())
    }

    private companion object {
        val KEY_ITEMS = stringPreferencesKey("items")
        const val MAX_ITEMS = 200
    }
}

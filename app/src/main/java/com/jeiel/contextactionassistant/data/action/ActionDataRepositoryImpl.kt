package com.jeiel.contextactionassistant.data.action

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
class ActionDataRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json
) : ActionDataRepository {

    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("action_data_store") }
    )

    override suspend fun saveTodo(item: TodoItem) {
        val current = readList<TodoItem>(KEY_TODOS).toMutableList()
        current.add(0, item)
        writeList(KEY_TODOS, current.take(MAX_ITEMS))
    }

    override suspend fun saveReceipt(item: ReceiptItem) {
        val current = readList<ReceiptItem>(KEY_RECEIPTS).toMutableList()
        current.add(0, item)
        writeList(KEY_RECEIPTS, current.take(MAX_ITEMS))
    }

    override suspend fun isDuplicateScheduleKey(key: String): Boolean {
        val keys = readList<String>(KEY_SCHEDULE_KEYS)
        return keys.contains(key)
    }

    override suspend fun markScheduleKey(key: String) {
        val keys = readList<String>(KEY_SCHEDULE_KEYS).toMutableList()
        if (!keys.contains(key)) {
            keys.add(0, key)
            writeList(KEY_SCHEDULE_KEYS, keys.take(MAX_ITEMS))
        }
    }

    private suspend inline fun <reified T> readList(key: androidx.datastore.preferences.core.Preferences.Key<String>): List<T> {
        val raw = dataStore.data.first()[key] ?: "[]"
        return runCatching { json.decodeFromString<List<T>>(raw) }.getOrDefault(emptyList())
    }

    private suspend inline fun <reified T> writeList(key: androidx.datastore.preferences.core.Preferences.Key<String>, data: List<T>) {
        dataStore.edit { pref ->
            pref[key] = json.encodeToString(data)
        }
    }

    private companion object {
        val KEY_TODOS = stringPreferencesKey("todos")
        val KEY_RECEIPTS = stringPreferencesKey("receipts")
        val KEY_SCHEDULE_KEYS = stringPreferencesKey("schedule_keys")
        const val MAX_ITEMS = 200
    }
}

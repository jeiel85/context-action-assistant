package com.jeiel.contextactionassistant.data.action

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: Long,
    val title: String,
    val memo: String,
    val dueDate: String,
    val priority: String,
    val createdAt: Long
)

@Serializable
data class ReceiptItem(
    val id: Long,
    val merchant: String,
    val paidAt: String,
    val amount: String,
    val currency: String,
    val paymentMethod: String,
    val createdAt: Long
)

interface ActionDataRepository {
    suspend fun saveTodo(item: TodoItem)
    suspend fun saveReceipt(item: ReceiptItem)
    suspend fun isDuplicateScheduleKey(key: String): Boolean
    suspend fun markScheduleKey(key: String)
}

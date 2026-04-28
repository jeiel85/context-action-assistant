package com.jeiel.contextactionassistant.data.review

import com.jeiel.contextactionassistant.domain.model.ActionType
import kotlinx.serialization.Serializable

@Serializable
data class ReviewItem(
    val id: Long,
    val type: ActionType,
    val confidence: Double,
    val summary: String,
    val createdAt: Long,
    val source: String,
    val data: Map<String, String> = emptyMap()
)

interface ReviewRepository {
    suspend fun add(item: ReviewItem)
    suspend fun clear()
    suspend fun getAll(): List<ReviewItem>
}

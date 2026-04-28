package com.jeiel.contextactionassistant.action

import com.jeiel.contextactionassistant.data.action.ReceiptItem
import com.jeiel.contextactionassistant.data.action.TodoItem
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ActionPayloadBuilder {
    fun scheduleKey(result: AiAnalysisResult): String {
        val title = result.data["title"].orEmpty()
        val date = result.data["date"].orEmpty()
        val startTime = result.data["startTime"].orEmpty()
        return "$title|$date|$startTime"
    }

    fun toTodo(result: AiAnalysisResult, now: Long): TodoItem {
        return TodoItem(
            id = now,
            title = result.data["title"] ?: result.summary.take(40),
            memo = result.data["memo"] ?: result.summary,
            dueDate = result.data["dueDate"].orEmpty(),
            priority = result.data["priority"] ?: "MEDIUM",
            createdAt = now
        )
    }

    fun toReceipt(result: AiAnalysisResult, now: Long): ReceiptItem {
        return ReceiptItem(
            id = now,
            merchant = result.data["merchant"] ?: "UNKNOWN",
            paidAt = result.data["paidAt"].orEmpty(),
            amount = result.data["amount"].orEmpty(),
            currency = result.data["currency"] ?: "KRW",
            paymentMethod = result.data["paymentMethod"].orEmpty(),
            createdAt = now
        )
    }

    fun receiptCsv(item: ReceiptItem): String {
        return buildString {
            appendLine("merchant,paidAt,amount,currency,paymentMethod")
            appendLine(
                listOf(item.merchant, item.paidAt, item.amount, item.currency, item.paymentMethod)
                    .joinToString(",") { escapeCsv(it) }
            )
        }
    }

    fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    fun scheduleStartMillisOrNull(result: AiAnalysisResult): Long? {
        val date = result.data["date"] ?: return null
        val startTime = result.data["startTime"] ?: return null
        val parsed = runCatching {
            LocalDateTime.parse("${date}T$startTime", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        }.getOrNull() ?: return null
        return parsed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

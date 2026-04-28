package com.jeiel.contextactionassistant.action

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.jeiel.contextactionassistant.data.action.ActionDataRepository
import com.jeiel.contextactionassistant.data.action.ReceiptItem
import com.jeiel.contextactionassistant.data.action.TodoItem
import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Singleton
class ActionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val actionDataRepository: ActionDataRepository
) {

    /*
     * Input: AiAnalysisResult
     * Output: Boolean
     * 핵심 로직: 결과 타입별로 클립보드 복사 또는 캘린더 인서트 Intent 실행
     * 이 로직을 작성한 이유: Action Card에서 즉시 실행 가능한 최소 액션을 MVP 범위로 제공하기 위해
     */
    fun executePrimaryAction(result: AiAnalysisResult): Boolean {
        return when (result.type) {
            ActionType.CODE, ActionType.NOTE -> copyToClipboard(result.summary)
            ActionType.RECEIPT -> saveReceiptAndCopyCsv(result)
            ActionType.SCHEDULE -> openCalendarInsert(result)
            ActionType.TODO -> saveTodo(result)
            else -> false
        }
    }

    private fun copyToClipboard(text: String): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("context_action", text))
        return true
    }

    private fun openCalendarInsert(result: AiAnalysisResult): Boolean {
        val title = result.data["title"] ?: "Context Action 일정"
        val date = result.data["date"].orEmpty()
        val startTime = result.data["startTime"].orEmpty()
        val scheduleKey = "$title|$date|$startTime"
        val duplicated = runBlocking { actionDataRepository.isDuplicateScheduleKey(scheduleKey) }
        if (duplicated) return false

        val beginMillis = System.currentTimeMillis() + 3_600_000
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        runBlocking { actionDataRepository.markScheduleKey(scheduleKey) }
        return true
    }

    private fun saveTodo(result: AiAnalysisResult): Boolean {
        val item = TodoItem(
            id = System.currentTimeMillis(),
            title = result.data["title"] ?: result.summary.take(40),
            memo = result.data["memo"] ?: result.summary,
            dueDate = result.data["dueDate"].orEmpty(),
            priority = result.data["priority"] ?: "MEDIUM",
            createdAt = System.currentTimeMillis()
        )
        runBlocking { actionDataRepository.saveTodo(item) }
        return copyToClipboard("${item.title}\n${item.memo}")
    }

    private fun saveReceiptAndCopyCsv(result: AiAnalysisResult): Boolean {
        val receipt = ReceiptItem(
            id = System.currentTimeMillis(),
            merchant = result.data["merchant"] ?: "UNKNOWN",
            paidAt = result.data["paidAt"].orEmpty(),
            amount = result.data["amount"].orEmpty(),
            currency = result.data["currency"] ?: "KRW",
            paymentMethod = result.data["paymentMethod"].orEmpty(),
            createdAt = System.currentTimeMillis()
        )
        runBlocking { actionDataRepository.saveReceipt(receipt) }
        val csv = buildString {
            appendLine("merchant,paidAt,amount,currency,paymentMethod")
            appendLine(
                listOf(
                    receipt.merchant,
                    receipt.paidAt,
                    receipt.amount,
                    receipt.currency,
                    receipt.paymentMethod
                ).joinToString(",") { escapeCsv(it) }
            )
        }
        return copyToClipboard(csv)
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}

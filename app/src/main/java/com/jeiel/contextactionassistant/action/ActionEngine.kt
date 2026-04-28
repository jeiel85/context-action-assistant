package com.jeiel.contextactionassistant.action

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /*
     * Input: AiAnalysisResult
     * Output: Boolean
     * 핵심 로직: 결과 타입별로 클립보드 복사 또는 캘린더 인서트 Intent 실행
     * 이 로직을 작성한 이유: Action Card에서 즉시 실행 가능한 최소 액션을 MVP 범위로 제공하기 위해
     */
    fun executePrimaryAction(result: AiAnalysisResult): Boolean {
        return when (result.type) {
            ActionType.CODE, ActionType.NOTE, ActionType.RECEIPT -> copyToClipboard(result.summary)
            ActionType.SCHEDULE -> openCalendarInsert(result)
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
        val beginMillis = System.currentTimeMillis() + 3_600_000
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginMillis)
            putExtra(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return true
    }
}

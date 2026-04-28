package com.jeiel.contextactionassistant.action

import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionPayloadBuilderTest {

    @Test
    fun `schedule key includes title date and start time`() {
        val result = AiAnalysisResult(
            type = ActionType.SCHEDULE,
            confidence = 0.9,
            summary = "summary",
            data = mapOf("title" to "회의", "date" to "2026-05-01", "startTime" to "09:00")
        )

        val key = ActionPayloadBuilder.scheduleKey(result)

        assertEquals("회의|2026-05-01|09:00", key)
    }

    @Test
    fun `receipt csv escapes quotes`() {
        val result = AiAnalysisResult(
            type = ActionType.RECEIPT,
            confidence = 0.9,
            summary = "summary",
            data = mapOf("merchant" to "A\"B", "amount" to "12000")
        )
        val item = ActionPayloadBuilder.toReceipt(result, 1L)

        val csv = ActionPayloadBuilder.receiptCsv(item)

        assertTrue(csv.contains("\"A\"\"B\""))
    }
}

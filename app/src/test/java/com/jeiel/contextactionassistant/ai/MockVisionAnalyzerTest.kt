package com.jeiel.contextactionassistant.ai

import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AnalysisRequest
import com.jeiel.contextactionassistant.domain.model.CaptureSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockVisionAnalyzerTest {

    private val analyzer = MockVisionAnalyzer()

    @Test
    fun `screenshot source returns note type`() = runTest {
        val request = AnalysisRequest(
            imageBytes = ByteArray(64),
            source = CaptureSource.SCREENSHOT_DETECTED
        )

        val result = analyzer.analyze(request).getOrThrow()

        assertEquals(ActionType.NOTE, result.type)
        assertTrue(result.confidence > 0.0)
    }

    @Test
    fun `large image bytes returns receipt type`() = runTest {
        val request = AnalysisRequest(
            imageBytes = ByteArray(230_000),
            source = CaptureSource.MANUAL_CAPTURE
        )

        val result = analyzer.analyze(request).getOrThrow()

        assertEquals(ActionType.RECEIPT, result.type)
    }

    @Test
    fun `small manual image returns code type`() = runTest {
        val request = AnalysisRequest(
            imageBytes = ByteArray(1024),
            source = CaptureSource.MANUAL_CAPTURE
        )

        val result = analyzer.analyze(request).getOrThrow()

        assertEquals(ActionType.CODE, result.type)
    }
}

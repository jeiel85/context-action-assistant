package com.jeiel.contextactionassistant.ai

import com.jeiel.contextactionassistant.domain.model.ActionItem
import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import com.jeiel.contextactionassistant.domain.model.AnalysisRequest

interface VisionAnalyzer {
    suspend fun analyze(request: AnalysisRequest): Result<AiAnalysisResult>
}

/*
 * Input: AnalysisRequest
 * Output: Result<AiAnalysisResult>
 * 핵심 로직: 실제 Vision API 대신 source와 바이트 크기를 기준으로 타입/신뢰도를 추정해 JSON 형태의 결과를 생성
 * 이 로직을 작성한 이유: MVP 초기 단계에서 네트워크 의존성 없이 전체 파이프라인을 먼저 연결하기 위해
 */
class MockVisionAnalyzer : VisionAnalyzer {
    override suspend fun analyze(request: AnalysisRequest): Result<AiAnalysisResult> {
        val type = when {
            request.source.name.contains("SCREENSHOT") -> ActionType.NOTE
            request.imageBytes.size > 220_000 -> ActionType.RECEIPT
            else -> ActionType.CODE
        }

        val summary = when (type) {
            ActionType.RECEIPT -> "영수증 후보를 감지했습니다. 금액/결제일을 확인하세요."
            ActionType.CODE -> "코드 블록 후보를 감지했습니다. 복사 전 언어를 확인하세요."
            ActionType.NOTE -> "화면 내용을 요약해 메모로 저장할 수 있습니다."
            else -> "액션 후보를 찾았습니다."
        }

        val actions = when (type) {
            ActionType.RECEIPT -> listOf(ActionItem("receipt_save", "영수증 저장", true))
            ActionType.CODE -> listOf(ActionItem("code_copy", "코드 복사", true))
            ActionType.NOTE -> listOf(ActionItem("note_save", "메모 저장", true))
            else -> listOf(ActionItem("noop", "결과 확인", true))
        }

        return Result.success(
            AiAnalysisResult(
                type = type,
                confidence = 0.74,
                summary = summary,
                data = mapOf("byteSize" to request.imageBytes.size.toString()),
                actions = actions
            )
        )
    }
}

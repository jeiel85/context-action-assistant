package com.jeiel.contextactionassistant.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import com.jeiel.contextactionassistant.ai.VisionAnalyzer
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import com.jeiel.contextactionassistant.domain.model.AnalysisRequest
import com.jeiel.contextactionassistant.domain.model.CaptureSource
import com.jeiel.contextactionassistant.pipeline.ImagePreprocessor
import javax.inject.Inject

class AnalyzeImageUseCase @Inject constructor(
    private val preprocessor: ImagePreprocessor,
    private val analyzer: VisionAnalyzer
) {

    /*
     * Input: ContentResolver, Uri, CaptureSource, appPackage(String?)
     * Output: Result<AiAnalysisResult>
     * 핵심 로직: 이미지 전처리 후 Vision Analyzer에 분석 요청을 전달하고 결과를 그대로 반환
     * 이 로직을 작성한 이유: 캡처 입력 종류와 AI 분석 로직을 분리해 Capture Layer와 Action Layer를 느슨하게 결합하기 위해
     */
    suspend operator fun invoke(
        contentResolver: ContentResolver,
        uri: Uri,
        source: CaptureSource,
        appPackage: String? = null
    ): Result<AiAnalysisResult> {
        val processed = preprocessor.process(contentResolver, uri).getOrElse { return Result.failure(it) }
        val request = AnalysisRequest(
            imageBytes = processed.bytes,
            source = source,
            appPackage = appPackage
        )
        return analyzer.analyze(request)
    }
}

package com.jeiel.contextactionassistant.ai

import com.jeiel.contextactionassistant.BuildConfig
import com.jeiel.contextactionassistant.domain.model.ActionItem
import com.jeiel.contextactionassistant.domain.model.ActionType
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import com.jeiel.contextactionassistant.domain.model.AnalysisRequest
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class GeminiVisionAnalyzer @Inject constructor(
    private val json: Json,
    private val client: OkHttpClient
) : VisionAnalyzer {

    override suspend fun analyze(request: AnalysisRequest): Result<AiAnalysisResult> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("GEMINI_API_KEY is empty"))
        }

        return runCatching {
            val prompt = """
                Analyze this image and return exactly one JSON object only.
                Schema:
                {
                  "type": "SCHEDULE|CODE|RECEIPT|NOTE|TODO|UNKNOWN",
                  "confidence": 0.0,
                  "summary": "string",
                  "data": {"k":"v"}
                }
            """.trimIndent()

            val payload = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(
                                inlineData = GeminiInlineData(
                                    mimeType = "image/jpeg",
                                    data = Base64.getEncoder().encodeToString(request.imageBytes)
                                )
                            )
                        )
                    )
                )
            )

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${BuildConfig.GEMINI_API_KEY}"
            val body = json.encodeToString(GeminiRequest.serializer(), payload)
                .toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder().url(endpoint).post(body).build()
            val raw = executeWithRetry(httpRequest)
            val parsed = json.decodeFromString(GeminiResponse.serializer(), raw)
            val text = parsed.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
            val cleanJson = text.substringAfter('{', "").let { "{$it" }.substringBeforeLast('}', "").let { "$it}" }
            val minimal = json.decodeFromString(MinimalResult.serializer(), cleanJson)

            AiAnalysisResult(
                type = minimal.type.toActionType(),
                confidence = minimal.confidence,
                summary = minimal.summary,
                data = minimal.data,
                actions = listOf(ActionItem("primary", "실행", true))
            )
        }
    }

    private suspend fun executeWithRetry(request: Request): String {
        var attempt = 0
        var lastError: Throwable? = null
        while (attempt < 3) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    error("Gemini error: ${response.code}")
                }
                return response.body?.string().orEmpty()
            } catch (t: Throwable) {
                lastError = t
                attempt += 1
                if (attempt < 3) {
                    delay(300L * attempt)
                }
            }
        }
        throw lastError ?: IllegalStateException("Gemini request failed")
    }

    private fun String.toActionType(): ActionType {
        return runCatching { ActionType.valueOf(this) }.getOrDefault(ActionType.UNKNOWN)
    }
}

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: GeminiInlineData? = null
)

@Serializable
private data class GeminiInlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
private data class GeminiResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

@Serializable
private data class GeminiCandidate(
    val content: GeminiContentOut
)

@Serializable
private data class GeminiContentOut(
    val parts: List<GeminiTextPart> = emptyList()
)

@Serializable
private data class GeminiTextPart(
    val text: String = ""
)

@Serializable
private data class MinimalResult(
    val type: String = "UNKNOWN",
    val confidence: Double = 0.0,
    val summary: String = "분석 결과 없음",
    val data: Map<String, String> = emptyMap()
)

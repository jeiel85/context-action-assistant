package com.jeiel.contextactionassistant.pipeline

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.jeiel.contextactionassistant.domain.model.ProcessedImage
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

@Singleton
class ImagePreprocessor @Inject constructor() {

    /*
     * Input: ContentResolver, Uri
     * Output: Result<ProcessedImage>
     * 핵심 로직: Uri를 디코딩한 뒤 긴 변 기준 1280px로 축소하고 JPEG로 압축 후 SHA-256 해시를 계산
     * 이 로직을 작성한 이유: Vision 전송 비용과 지연을 줄이고 중복 감지를 위한 안정적인 식별자를 만들기 위해
     */
    suspend fun process(contentResolver: ContentResolver, uri: Uri): Result<ProcessedImage> = withContext(Dispatchers.IO) {
        runCatching {
            val src = ImageDecoder.createSource(contentResolver, uri)
            val original = ImageDecoder.decodeBitmap(src)
            val resized = resizeToMaxSide(original, 1280)
            val output = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 85, output)
            if (resized != original) {
                original.recycle()
                resized.recycle()
            } else {
                original.recycle()
            }
            val bytes = output.toByteArray()
            ProcessedImage(bytes = bytes, sha256 = bytes.sha256())
        }
    }

    private fun resizeToMaxSide(bitmap: Bitmap, maxSide: Int): Bitmap {
        val maxCurrent = max(bitmap.width, bitmap.height)
        if (maxCurrent <= maxSide) return bitmap
        val scale = maxSide.toFloat() / maxCurrent.toFloat()
        val targetW = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val targetH = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun ByteArray.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(this)
        return digest.joinToString("") { "%02x".format(it) }
    }
}

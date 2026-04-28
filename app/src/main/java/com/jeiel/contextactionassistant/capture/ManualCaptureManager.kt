package com.jeiel.contextactionassistant.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

@Singleton
class ManualCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createCaptureIntent(): Intent {
        val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return mgr.createScreenCaptureIntent()
    }

    /*
     * Input: resultCode(Int), data(Intent), width(Int), height(Int), density(Int)
     * Output: Result<Bitmap>
     * 핵심 로직: MediaProjection으로 VirtualDisplay/ImageReader를 생성해 단일 프레임을 수신 후 Bitmap으로 변환
     * 이 로직을 작성한 이유: 사용자 명시 승인 기반 수동 캡처 MVP 요구사항을 Play Store 정책 내에서 충족하기 위해
     */
    suspend fun captureOnce(
        resultCode: Int,
        data: Intent,
        width: Int,
        height: Int,
        densityDpi: Int
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            val mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            try {
                captureBitmap(mediaProjection, width, height, densityDpi)
            } finally {
                mediaProjection.stop()
            }
        }
    }

    private suspend fun captureBitmap(
        mediaProjection: MediaProjection,
        width: Int,
        height: Int,
        densityDpi: Int
    ): Bitmap = suspendCancellableCoroutine { cont ->
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        var virtualDisplay: VirtualDisplay? = null

        val listener = ImageReader.OnImageAvailableListener {
            val image: Image? = reader.acquireLatestImage()
            if (image != null) {
                val bitmap = image.toBitmap(width, height)
                image.close()
                reader.setOnImageAvailableListener(null, null)
                virtualDisplay?.release()
                reader.close()
                if (cont.isActive) cont.resume(bitmap)
            }
        }

        reader.setOnImageAvailableListener(listener, Handler(Looper.getMainLooper()))
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "context-action-capture",
            width,
            height,
            densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader.surface,
            null,
            null
        )

        cont.invokeOnCancellation {
            reader.setOnImageAvailableListener(null, null)
            virtualDisplay?.release()
            reader.close()
        }
    }

    private fun Image.toBitmap(width: Int, height: Int): Bitmap {
        val plane = planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height).also {
            bitmap.recycle()
        }
    }
}

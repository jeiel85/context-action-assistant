package com.jeiel.contextactionassistant.capture

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class ScreenshotDetector @Inject constructor() {

    /*
     * Input: ContentResolver
     * Output: Flow<Uri>
     * 핵심 로직: MediaStore 이미지 변경을 감지하고 최근 1건 Uri를 조회해 스크린샷 패턴일 때만 Flow로 방출
     * 이 로직을 작성한 이유: API 33 이하 MVP에서 공식 ScreenCaptureCallback 대체 경로가 필요하기 때문
     */
    fun observe(contentResolver: ContentResolver): Flow<Uri> = callbackFlow {
        val worker = HandlerThread("screenshot-observer-thread").apply { start() }
        val observer = object : ContentObserver(Handler(worker.looper)) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                val latest = queryLatestScreenshotUri(contentResolver)
                if (latest != null) {
                    trySend(latest)
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
            worker.quitSafely()
        }
    }

    private fun queryLatestScreenshotUri(contentResolver: ContentResolver): Uri? {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            if (!cursor.moveToFirst()) return null
            val name = cursor.getString(1)?.lowercase().orEmpty()
            val path = cursor.getString(2)?.lowercase().orEmpty()
            val isScreenshot = name.contains("screenshot") || path.contains("screenshot") || path.contains("screenshots")
            if (!isScreenshot) return null
            val id = cursor.getLong(0)
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
        }
        return null
    }
}

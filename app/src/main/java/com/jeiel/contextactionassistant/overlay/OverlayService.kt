package com.jeiel.contextactionassistant.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.jeiel.contextactionassistant.R
import com.jeiel.contextactionassistant.MainActivity
import com.jeiel.contextactionassistant.action.ActionEngine
import com.jeiel.contextactionassistant.capture.ScreenshotDetector
import com.jeiel.contextactionassistant.data.review.ReviewItem
import com.jeiel.contextactionassistant.data.review.ReviewRepository
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import com.jeiel.contextactionassistant.domain.model.CaptureSource
import com.jeiel.contextactionassistant.domain.usecase.AnalyzeImageUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OverlayService : LifecycleService() {

    @Inject lateinit var screenshotDetector: ScreenshotDetector
    @Inject lateinit var analyzeImageUseCase: AnalyzeImageUseCase
    @Inject lateinit var actionEngine: ActionEngine
    @Inject lateinit var reviewRepository: ReviewRepository

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var cardView: View? = null
    private var detectorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startOverlay()
            ACTION_STOP -> stopOverlay()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        detectorJob?.cancel()
        removeView(bubbleView)
        removeView(cardView)
        super.onDestroy()
    }

    private fun startOverlay() {
        startForeground(NOTIFICATION_ID, buildNotification("오버레이 실행 중"))
        if (!Settings.canDrawOverlays(this)) return
        if (bubbleView == null) {
            addBubble()
        }
        if (detectorJob == null) {
            detectorJob = lifecycleScope.launch {
                screenshotDetector.observe(contentResolver).collect { uri ->
                    val result = analyzeImageUseCase(
                        contentResolver = contentResolver,
                        uri = uri,
                        source = CaptureSource.SCREENSHOT_DETECTED
                    ).getOrNull() ?: return@collect
                    reviewRepository.add(
                        ReviewItem(
                            id = System.currentTimeMillis(),
                            type = result.type,
                            confidence = result.confidence,
                            summary = result.summary,
                            createdAt = System.currentTimeMillis(),
                            source = CaptureSource.SCREENSHOT_DETECTED.name
                        )
                    )
                    if (result.confidence >= 0.7) {
                        showActionCard(result)
                    }
                }
            }
        }
    }

    private fun stopOverlay() {
        detectorJob?.cancel()
        detectorJob = null
        removeView(bubbleView)
        removeView(cardView)
        bubbleView = null
        cardView = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addBubble() {
        val composeView = ComposeView(this).apply {
            ViewTreeLifecycleOwner.set(this, this@OverlayService)
            setContent {
                BubbleUi(
                    onClick = {
                        val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                            action = MainActivity.ACTION_MANUAL_CAPTURE
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                    }
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 60
            y = 300
        }

        windowManager.addView(composeView, params)
        bubbleView = composeView
    }

    private fun showActionCard(result: AiAnalysisResult) {
        removeView(cardView)
        val composeView = ComposeView(this).apply {
            ViewTreeLifecycleOwner.set(this, this@OverlayService)
            setContent {
                ActionCardUi(
                    result = result,
                    onPrimaryAction = {
                        actionEngine.executePrimaryAction(result)
                        removeView(cardView)
                        cardView = null
                    },
                    onDismiss = {
                        removeView(cardView)
                        cardView = null
                    }
                )
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = 80
        }

        windowManager.addView(composeView, params)
        cardView = composeView
    }

    private fun removeView(view: View?) {
        if (view != null && view.parent != null) {
            windowManager.removeView(view)
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(content)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val ACTION_START = "overlay.action.START"
        const val ACTION_STOP = "overlay.action.STOP"
        private const val CHANNEL_ID = "overlay_service"
        private const val NOTIFICATION_ID = 2011
    }
}

@Composable
private fun BubbleUi(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E88E5))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = "AI", color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ActionCardUi(
    result: AiAnalysisResult,
    onPrimaryAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "${result.type} (${result.confidence})", style = MaterialTheme.typography.titleMedium)
            Text(text = result.summary)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPrimaryAction) { Text("실행") }
                Button(onClick = onDismiss) { Text("닫기") }
            }
        }
    }
}

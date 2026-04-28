package com.jeiel.contextactionassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.jeiel.contextactionassistant.action.ActionEngine
import com.jeiel.contextactionassistant.capture.ManualCaptureManager
import com.jeiel.contextactionassistant.core.permission.PermissionManager
import com.jeiel.contextactionassistant.data.review.ReviewItem
import com.jeiel.contextactionassistant.domain.model.CaptureSource
import com.jeiel.contextactionassistant.domain.usecase.AnalyzeImageUseCase
import com.jeiel.contextactionassistant.ui.home.HomeScreen
import com.jeiel.contextactionassistant.ui.home.HomeViewModel
import com.jeiel.contextactionassistant.ui.theme.ContextActionAssistantTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var manualCaptureManager: ManualCaptureManager
    @Inject lateinit var analyzeImageUseCase: AnalyzeImageUseCase
    @Inject lateinit var actionEngine: ActionEngine
    @Inject lateinit var permissionManager: PermissionManager

    private val viewModel: HomeViewModel by viewModels()
    private val captureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) {
            viewModel.setLatestMessage("캡처 권한이 거부되었습니다.")
            viewModel.setAnalyzing(false)
            return@registerForActivityResult
        }

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1)
        val height = metrics.heightPixels.coerceAtLeast(1)
        val density = metrics.densityDpi.coerceAtLeast(1)

        lifecycleScope.launch {
            val bitmap = manualCaptureManager.captureOnce(
                resultCode = result.resultCode,
                data = result.data!!,
                width = width,
                height = height,
                densityDpi = density
            ).getOrElse {
                viewModel.setLatestMessage("캡처 실패: ${it.message}")
                viewModel.setAnalyzing(false)
                return@launch
            }

            val analysis = analyzeImageUseCase(
                bitmap = bitmap,
                source = CaptureSource.MANUAL_CAPTURE
            ).getOrElse {
                viewModel.setLatestMessage("분석 실패: ${it.message}")
                viewModel.setAnalyzing(false)
                bitmap.recycle()
                return@launch
            }

            bitmap.recycle()
            actionEngine.executePrimaryAction(analysis)
            viewModel.setLatestMessage("분석 완료: ${analysis.type} (${analysis.confidence})")
            viewModel.setAnalyzing(false)
        }
    }
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSharedImage(intent)
        handleCaptureRequest(intent)

        setContent {
            ContextActionAssistantTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    state = state,
                    onStartOverlay = { viewModel.startOverlayService(this) },
                    onStopOverlay = { viewModel.stopOverlayService(this) },
                    onRequestOverlayPermission = { viewModel.openOverlayPermissionSettings(this) },
                    onToggleAiTransfer = viewModel::setAiTransferEnabled,
                    onManualCapture = {
                        viewModel.setAnalyzing(true)
                        captureLauncher.launch(manualCaptureManager.createCaptureIntent())
                    },
                    onRefreshReviews = viewModel::refreshReviews,
                    onClearReviews = viewModel::clearReviews,
                    onRequestRuntimePermissions = {
                        val missing = permissionManager.missingRuntimePermissions()
                        if (missing.isNotEmpty()) {
                            permissionLauncher.launch(missing.toTypedArray())
                        }
                    },
                    onExecuteReviewItem = { reviewId ->
                        val item = viewModel.uiState.value.reviewItems.firstOrNull { it.id == reviewId }
                        if (item != null) {
                            viewModel.executeReviewItem(item)
                        }
                    },
                    onReviewQueryChange = viewModel::setReviewQuery,
                    onReviewFilterChange = viewModel::setReviewFilter,
                    onToggleExpandedReview = viewModel::toggleExpandedReview
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedImage(intent)
        handleCaptureRequest(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshOverlayPermission(this)
        viewModel.refreshReviews()
        viewModel.refreshPermissions()
    }

    private fun handleSharedImage(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                lifecycleScope.launch {
                    viewModel.setAnalyzing(true)
                    val analysis = analyzeImageUseCase(
                        contentResolver = contentResolver,
                        uri = uri,
                        source = CaptureSource.SHARED_IMAGE
                    ).getOrElse {
                        viewModel.setLatestMessage("공유 이미지 분석 실패: ${it.message}")
                        viewModel.setAnalyzing(false)
                        return@launch
                    }
                    val item = ReviewItem(
                        id = System.currentTimeMillis(),
                        type = analysis.type,
                        confidence = analysis.confidence,
                        summary = analysis.summary,
                        createdAt = System.currentTimeMillis(),
                        source = CaptureSource.SHARED_IMAGE.name,
                        data = analysis.data
                    )
                    viewModel.addReviewItem(item)
                    actionEngine.executePrimaryAction(analysis)
                    viewModel.setLatestMessage("공유 이미지 분석 완료: ${analysis.type}")
                    viewModel.setAnalyzing(false)
                }
            }
        }
    }

    private fun handleCaptureRequest(intent: Intent?) {
        if (intent?.action == ACTION_MANUAL_CAPTURE) {
            viewModel.setAnalyzing(true)
            captureLauncher.launch(manualCaptureManager.createCaptureIntent())
        }
    }

    companion object {
        const val ACTION_MANUAL_CAPTURE = "com.jeiel.contextactionassistant.action.MANUAL_CAPTURE"
    }
}

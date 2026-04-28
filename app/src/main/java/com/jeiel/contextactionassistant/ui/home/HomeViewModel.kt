package com.jeiel.contextactionassistant.ui.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeiel.contextactionassistant.action.ActionEngine
import com.jeiel.contextactionassistant.core.permission.PermissionManager
import com.jeiel.contextactionassistant.data.datastore.SettingsRepository
import com.jeiel.contextactionassistant.data.review.ReviewItem
import com.jeiel.contextactionassistant.data.review.ReviewRepository
import com.jeiel.contextactionassistant.domain.model.AiAnalysisResult
import com.jeiel.contextactionassistant.domain.model.PrivacyFlags
import com.jeiel.contextactionassistant.overlay.OverlayService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reviewRepository: ReviewRepository,
    private val permissionManager: PermissionManager,
    private val actionEngine: ActionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(
                        overlayEnabled = settings.overlayEnabled,
                        aiTransferEnabled = settings.aiTransferEnabled
                    )
                }
            }
        }
        refreshReviews()
        refreshPermissions()
    }

    fun startOverlayService(context: Context) {
        context.startForegroundService(Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_START
        })
        viewModelScope.launch {
            settingsRepository.setOverlayEnabled(true)
        }
    }

    fun stopOverlayService(context: Context) {
        context.startService(Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_STOP
        })
        viewModelScope.launch {
            settingsRepository.setOverlayEnabled(false)
        }
    }

    fun openOverlayPermissionSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }

    fun setAiTransferEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAiTransferEnabled(enabled)
        }
    }

    fun setAnalyzing(analyzing: Boolean) {
        _uiState.update { it.copy(isAnalyzing = analyzing) }
    }

    fun setLatestMessage(message: String) {
        _uiState.update { it.copy(latestMessage = message) }
    }

    fun refreshOverlayPermission(context: Context) {
        val granted = Settings.canDrawOverlays(context)
        _uiState.update { it.copy(overlayPermissionGranted = granted) }
    }

    fun refreshReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(reviewItems = reviewRepository.getAll()) }
        }
    }

    fun clearReviews() {
        viewModelScope.launch {
            reviewRepository.clear()
            _uiState.update { it.copy(reviewItems = emptyList()) }
        }
    }

    fun refreshPermissions() {
        _uiState.update { it.copy(missingPermissions = permissionManager.missingRuntimePermissions()) }
    }

    fun addReviewItem(item: ReviewItem) {
        viewModelScope.launch {
            reviewRepository.add(item)
            refreshReviews()
        }
    }

    fun executeReviewItem(item: ReviewItem) {
        val result = AiAnalysisResult(
            type = item.type,
            confidence = item.confidence,
            summary = item.summary,
            data = item.data,
            privacyFlags = PrivacyFlags()
        )
        val success = actionEngine.executePrimaryAction(result)
        _uiState.update {
            it.copy(latestMessage = if (success) "검토 항목 실행 완료" else "검토 항목 실행 실패")
        }
    }
}

package com.jeiel.contextactionassistant.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeiel.contextactionassistant.data.datastore.SettingsRepository
import com.jeiel.contextactionassistant.data.review.ReviewRepository
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
    private val reviewRepository: ReviewRepository
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

    fun onSharedImage(uri: Uri) {
        _uiState.update {
            it.copy(latestMessage = "공유 이미지 수신: $uri")
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
}

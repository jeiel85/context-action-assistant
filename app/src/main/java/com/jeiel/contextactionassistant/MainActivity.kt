package com.jeiel.contextactionassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jeiel.contextactionassistant.ui.home.HomeScreen
import com.jeiel.contextactionassistant.ui.home.HomeViewModel
import com.jeiel.contextactionassistant.ui.theme.ContextActionAssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleSharedImage(intent)

        setContent {
            ContextActionAssistantTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    state = state,
                    onStartOverlay = { viewModel.startOverlayService(this) },
                    onStopOverlay = { viewModel.stopOverlayService(this) },
                    onRequestOverlayPermission = { viewModel.openOverlayPermissionSettings(this) },
                    onToggleAiTransfer = viewModel::setAiTransferEnabled
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSharedImage(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshOverlayPermission(this)
    }

    private fun handleSharedImage(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            if (uri != null) {
                viewModel.onSharedImage(uri)
            }
        }
    }
}

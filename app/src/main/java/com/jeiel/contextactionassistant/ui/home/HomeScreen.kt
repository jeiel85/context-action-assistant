package com.jeiel.contextactionassistant.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    state: HomeUiState,
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onToggleAiTransfer: (Boolean) -> Unit,
    onManualCapture: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Context Action Assistant",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(text = "Play Store MVP 구조가 적용된 기본 앱입니다.")

        Card(modifier = Modifier.padding(top = 4.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "AI 전송 허용")
                Switch(checked = state.aiTransferEnabled, onCheckedChange = onToggleAiTransfer)
                Text(text = "최신 상태: ${state.latestMessage}")
            }
        }

        Button(onClick = onRequestOverlayPermission) {
            Text("오버레이 권한 열기")
        }

        Button(onClick = onStartOverlay) {
            Text("오버레이 시작")
        }

        Button(onClick = onManualCapture, enabled = !state.isAnalyzing) {
            Text(if (state.isAnalyzing) "분석 중..." else "수동 화면 캡처 분석")
        }

        Button(onClick = onStopOverlay) {
            Text("오버레이 중지")
        }
    }
}

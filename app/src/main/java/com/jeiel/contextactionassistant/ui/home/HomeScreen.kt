package com.jeiel.contextactionassistant.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onManualCapture: () -> Unit,
    onRefreshReviews: () -> Unit,
    onClearReviews: () -> Unit,
    onRequestRuntimePermissions: () -> Unit
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
                Text(text = "Batch Review: ${state.reviewItems.size}건")
                Text(text = "미승인 권한: ${state.missingPermissions.size}건")
            }
        }

        Button(onClick = onRequestRuntimePermissions) {
            Text("런타임 권한 요청")
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

        Button(onClick = onRefreshReviews) {
            Text("검토 목록 새로고침")
        }

        Button(onClick = onClearReviews) {
            Text("검토 목록 비우기")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.reviewItems.take(8), key = { it.id }) { item ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${item.type} (${item.confidence})")
                        Text(item.summary)
                    }
                }
            }
        }
    }
}

package com.jeiel.contextactionassistant.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
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
    onRequestRuntimePermissions: () -> Unit,
    onExecuteReviewItem: (Long) -> Unit,
    onReviewQueryChange: (String) -> Unit,
    onReviewFilterChange: (String) -> Unit,
    onToggleExpandedReview: (Long) -> Unit
) {
    val filteredItems = state.reviewItems.filter { item ->
        val typeMatched = state.reviewFilter == "ALL" || item.type.name == state.reviewFilter
        val query = state.reviewQuery.trim()
        val queryMatched = query.isEmpty() ||
            item.summary.contains(query, ignoreCase = true) ||
            item.source.contains(query, ignoreCase = true)
        typeMatched && queryMatched
    }

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

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.reviewQuery,
            onValueChange = onReviewQueryChange,
            label = { Text("검토 검색") },
            singleLine = true
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "SCHEDULE", "CODE", "RECEIPT", "NOTE", "TODO").forEach { filter ->
                Button(onClick = { onReviewFilterChange(filter) }) {
                    Text(if (state.reviewFilter == filter) "[$filter]" else filter)
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredItems.take(20), key = { it.id }) { item ->
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.type} (${item.confidence})")
                        Text(item.summary)
                        Button(onClick = { onExecuteReviewItem(item.id) }) {
                            Text("이 항목 실행")
                        }
                        Button(onClick = { onToggleExpandedReview(item.id) }) {
                            Text(if (state.expandedReviewId == item.id) "상세 닫기" else "상세 보기")
                        }
                        if (state.expandedReviewId == item.id) {
                            Text("source: ${item.source}")
                            Text("createdAt: ${item.createdAt}")
                            Text("data: ${item.data}")
                        }
                    }
                }
            }
        }
    }
}

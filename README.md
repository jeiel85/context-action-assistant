# Context Action Assistant

[![Android CI](https://github.com/jeiel85/context-action-assistant/actions/workflows/android-ci.yml/badge.svg)](https://github.com/jeiel85/context-action-assistant/actions/workflows/android-ci.yml)

> Capture context, convert to action.  
> 스크린샷/수동 캡처 화면을 AI가 분석해 일정, 코드, 메모, 영수증 등 실행 가능한 결과로 바꾸는 Android 오버레이 앱.

## Why This App
- 화면에서 본 정보를 앱 전환 없이 바로 액션으로 연결
- Play Store 배포 기준을 고려한 권한/보안 모델
- 자동 감지(스크린샷) + 수동 분석(MediaProjection) 통합 구조

## Current MVP Status
- `Compose + Material3 + Hilt + DataStore` 프로젝트 기반
- `OverlayService` 플로팅 버블 + 하단 액션 카드
- `ScreenshotDetector(ContentObserver)` 자동 감지 경로
- `MediaProjection` 기반 수동 1회 캡처 분석
- 공유 인텐트 이미지 즉시 분석 + 결과 저장
- `ImagePreprocessor` (리사이즈/압축)
- `VisionAnalyzer` 추상화 + `GeminiVisionAnalyzer` + Mock fallback
- Batch Review 기본 저장/조회(최신 분석 결과 목록)
- Batch Review 항목 재실행(저장된 결과 액션 재트리거)
- Action Engine 고도화(TODO 저장, RECEIPT CSV 클립보드, SCHEDULE 중복 키 방지)
- MockVisionAnalyzer 단위 테스트 추가
- Action payload/CSV 포맷 단위 테스트 추가

## Architecture
```text
Capture Sources
  - Screenshot Detection
  - Manual MediaProjection
  - Share Intent

Image Pipeline
  - Decode / Resize / Compress

AI Core
  - VisionAnalyzer (Gemini or Mock)

Action Engine
  - Copy / Calendar Insert / Result Action

Overlay UX
  - Floating Bubble
  - Bottom Action Card
  - Batch Review (Home)
```

## Tech Stack
- Kotlin, Coroutines, Flow
- Jetpack Compose, Material 3
- Hilt (DI)
- DataStore
- OkHttp + Kotlin Serialization

## Getting Started
### 1) Clone
```bash
git clone https://github.com/jeiel85/context-action-assistant.git
cd context-action-assistant
```

### 2) API Key (Optional)
`local.properties`:
```properties
GEMINI_API_KEY=your_key_here
```
- 키가 없거나 API 호출 실패 시 Mock 분석기로 자동 fallback 됩니다.

### 3) Open in Android Studio
- Android Studio Hedgehog+ 권장
- JDK 17

## Permissions Strategy (Play Store Oriented)
- `SYSTEM_ALERT_WINDOW`: 오버레이 표시
- `POST_NOTIFICATIONS`: 포그라운드 서비스 알림
- `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`: 스크린샷 감지
- `MediaProjection` 승인: 사용자 수동 캡처 시작 시점에만 요청
- `READ_CALENDAR`, `WRITE_CALENDAR`: 일정 등록 시에만 사용

## Security & Privacy Principles
- 원본 스크린샷 기본 미저장
- 민감 텍스트 로그 미기록
- 사용자 확인 없는 자동 액션 금지
- 기능별 권한 단계적 요청

## CI (GitHub Actions)
- Workflow: `.github/workflows/android-ci.yml`
- Trigger: `push`, `pull_request` on `main`
- Steps:
  - Assemble Debug APK
  - Run Unit Tests

## Roadmap
- [ ] Batch Review 전용 화면 고도화(필터/검색/상세)
- [ ] 액션 타입 고도화(TODO/RECEIPT/SCHEDULE 정밀 처리)
- [ ] 권한 거부/오류 UX 정교화
- [ ] 테스트 커버리지 확대 및 성능 점검

## Design Spec
- `Unified_Context_Action_Assistant_PlayStore_Spec_v1_1.md`

## License
- TBD

# Context Action Assistant

<p align="center">
  <img src="assets/ic_launcher.svg" width="120" alt="App Icon" />
</p>

<p align="center">
  <a href="https://github.com/jeiel85/context-action-assistant/actions/workflows/android-ci.yml">
    <img src="https://github.com/jeiel85/context-action-assistant/actions/workflows/android-ci.yml/badge.svg" alt="Android CI" />
  </a>
  <img src="https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-1.6.8-4285F4?logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material%203-6750A4?logo=materialdesign&logoColor=white" alt="Material 3" />
  <img src="https://img.shields.io/badge/Hilt-DI-FF4081?logo=dagger&logoColor=white" alt="Hilt" />
  <img src="https://img.shields.io/github/license/jeiel85/context-action-assistant?color=green" alt="License" />
</p>

> **Capture context, convert to action.**  
> 스크린샷/수동 캡처 화면을 AI가 분석해 일정, 코드, 메모, 영수증 등 실행 가능한 결과로 바꾸는 Android 오버레이 앱.

---

## 📱 다운로드

| 채널 | 상태 | 설명 |
|------|------|------|
| Play Store | *준비 중* | MVP 완성 후 배포 예정 |
| GitHub Releases | [![Latest Release](https://img.shields.io/github/v/release/jeiel85/context-action-assistant?include_prereleases&sort=semver)](https://github.com/jeiel85/context-action-assistant/releases) | 디버그 APK 다운로드 |

---

## ✨ 주요 기능

### 🔍 자동 스크린샷 감지
- 시스템 API를 활용한 스크린샷 생성 감지 (API 34+ `ScreenCaptureCallback`)
- 하위 버전은 `ContentObserver`로 `MediaStore` 모니터링
- 중복 감지 방지 (이미지 해시 캐싱)

### 🫧 플로팅 버블 (수동 분석)
- 화면 위 플로팅 버블로 현재 화면 즉시 분석
- 사용자 승인 기반 `MediaProjection` 캡처
- 드래그 이동 및 제스처 지원

### 🤖 AI 기반 컨텍스트 분석
- 캡처된 화면을 AI(Gemini Vision)가 분석
- 실행 가능한 액션 추출: 일정, 코드, 영수증, 메모, TODO
- 신뢰도 기반 액션 제안

### 📋 액션 카드 & 배치 리뷰
- 하단 슬라이드 업 카드로 액션 제안
- 저장된 결과 목록에서 재실행 가능
- 검색 및 타입 필터 지원

---

## 🏗 아키텍처

```
Capture Sources
  ├─ Screenshot Detection (API 34+ / 33-)
  ├─ Manual MediaProjection
  └─ Share Intent / Image Picker

Image Pipeline
  ├─ Decode → Resize → Compress
  ├─ Duplicate Check
  └─ Sensitive Data Guard

AI Core
  ├─ Prompt Builder + Context Injection
  ├─ VisionAnalyzer (Gemini / Mock)
  └─ JSON Response Parser

Action Engine
  ├─ Schedule / Code / Receipt
  ├─ Note / TODO / Unknown
  └─ Result Action (Copy / Calendar / Save)

Overlay UX
  ├─ Floating Bubble
  ├─ Bottom Action Card
  └─ Batch Review (Home)
```

---

## 🛠 기술 스택

| 영역 | 기술 |
|------|------|
| 언어 | Kotlin, Coroutines, Flow |
| UI | Jetpack Compose + Material Design 3 |
| DI | Hilt |
| 설정 저장 | DataStore |
| 네트워크 | OkHttp + Kotlin Serialization |
| 이미지 로딩 | Coil |
| 빌드 시스템 | Gradle 8.10.2 (Kotlin DSL) |

---

## 🚀 시작하기

### 1) 클론
```bash
git clone https://github.com/jeiel85/context-action-assistant.git
cd context-action-assistant
```

### 2) API Key 설정 (선택)
`local.properties` 파일에 Gemini API Key를 입력하세요:
```properties
GEMINI_API_KEY=your_key_here
```
> 키가 없거나 API 호출 실패 시 **Mock 분석기**로 자동 fallback 됩니다.

### 3) Android Studio에서 열기
- **Android Studio Hedgehog (2023.1.1)** 이상 권장
- **JDK 17** 필요

---

## 🔐 권한 전략 (Play Store 지향)

| 권한 | 목적 | 요청 시점 |
|------|------|----------|
| `SYSTEM_ALERT_WINDOW` | 오버레이 표시 | 플로팅 버블 활성화 시 |
| `POST_NOTIFICATIONS` | 포그라운드 서비스 알림 | Android 13+ 서비스 시작 시 |
| `READ_MEDIA_IMAGES` | 스크린샷 이미지 접근 | 자동 감지 활성화 시 |
| `MediaProjection` 승인 | 현재 화면 수동 캡처 | 사용자가 캡처 시작할 때 |
| `READ/WRITE_CALENDAR` | 일정 등록 | 일정 액션 실행 시 |

> **Play Store 배포 기준**: Shizuku, 접근성 서비스 기반 기능은 MVP에서 제외하고 별도 고급판으로 분리 예정

---

## 🛡 보안 & 프라이버시 원칙

- ✅ 원본 스크린샷 **기본 미저장**
- ✅ 민감 텍스트 로그 **미기록**
- ✅ 사용자 확인 없는 자동 액션 **금지**
- ✅ 기능별 권한 **단계적 요청**
- ✅ API Key 하드코딩 **금지** (`BuildConfig` + `local.properties`)

---

## 🤖 AI 액션 타입

| 타입 | 설명 | 예시 액션 |
|------|------|----------|
| `SCHEDULE` | 일정 등록 | "내일 3시 회의" → 캘린더 추가 |
| `CODE` | 코드 복사 | 코드 블록 추출 → 클립보드 복사 |
| `RECEIPT` | 영수증 정리 | 결제 정보 → CSV 내보내기 |
| `NOTE` | 메모 저장 | 화면 내용 → 제목+본문 메모 |
| `TODO` | 할 일 생성 | 화면에서 TODO 추출 → 저장 |

---

## 📦 CI/CD (GitHub Actions)

자동 빌드 및 테스트 워크플로우가 구성되어 있습니다:

- **Trigger**: `push`, `pull_request` → `main` 브랜치
- **Steps**:
  1. Gradle Build (`assembleDebug`)
  2. Unit Tests (`testDebugUnitTest`)
  3. CI 로그 아티팩트 업로드

> 워크플로우 파일: [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml)

---

## 🗺 로드맵

- [x] 기본 MVP 완성 (Compose + Hilt + DataStore)
- [x] OverlayService (플로팅 버블 + 액션 카드)
- [x] 스크린샷 자동 감지
- [x] MediaProjection 수동 캡처
- [x] Gemini Vision AI 통합
- [x] Batch Review (저장/조회/재실행)
- [x] 액션 엔진 고도화 (TODO, Receipt, Schedule)
- [ ] 배치 리뷰 UI 고도화 (필터/검색/상세)
- [ ] 액션 타입 정밀 처리
- [ ] 권한 거부/오류 UX 정교화
- [ ] 테스트 커버리지 확대
- [ ] Play Store 배포

---

## 📄 변경 이력

최신 변경사항은 [Releases](https://github.com/jeiel85/context-action-assistant/releases) 페이지를 참고하세요.

---

## 📋 설계 문서

상세 스펙은 [통합 설계서 v1.1](Unified_Context_Action_Assistant_PlayStore_Spec_v1_1.md)을 참고하세요.

---

## 📜 라이선스

현재 결정 중 (TBD).  
문의: [GitHub Issues](https://github.com/jeiel85/context-action-assistant/issues)

---

<p align="center">
  Made with ❤️ using <a href="https://developer.android.com/jetpack/compose">Jetpack Compose</a> + <a href="https://dagger.dev/hilt/">Hilt</a>
</p>

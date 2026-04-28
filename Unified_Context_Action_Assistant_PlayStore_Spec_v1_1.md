# Context Action Assistant 통합 설계서 v1.1 - Play Store 배포 기준

> 두 개의 기존 아이디어를 하나의 Android 네이티브 앱으로 통합한 개인정보 제거 버전입니다.  
> 이 버전은 **Google Play Store 배포 가능성**을 최우선으로 고려합니다.  
> 목표는 “사용자가 명시적으로 캡처하거나 직접 생성한 스크린샷을 AI가 액션 가능한 결과로 변환해주는 오버레이형 개인 생산성 도구”입니다.

---

## 1. 제품 정의

### 제품명
**Context Action Assistant**

### 한 줄 설명
사용자가 화면을 보다가 필요한 순간 또는 스크린샷을 찍은 순간, 화면 내용을 AI가 분석해 일정 등록, 코드 복사, 영수증 정리, 메모 생성 등으로 바로 이어주는 Android 오버레이 앱입니다.

### 핵심 방향
기존 두 앱의 아이디어를 다음처럼 통합합니다.

1. **자동 감지형 액션**
   - 사용자가 스크린샷을 찍으면 앱이 이를 감지합니다.
   - 이미지 내용을 분석해 액션 후보를 자동으로 제안합니다.
   - 예: 일정 등록, 코드 복사, 영수증 정리, TODO 생성, 메모 저장

2. **수동 호출형 컨텍스트 분석**
   - 화면 위 플로팅 버블을 사용자가 누르면 현재 화면을 캡처합니다.
   - AI가 현재 화면 맥락을 분석해 설명, 요약, 복사 가능한 결과, 다음 액션을 제공합니다.
   - Dex 모드, 태블릿 PC 모드, 분할 화면 환경에서 특히 유용하도록 설계합니다.

3. **공통 AI Action Layer**
   - 자동 스크린샷 감지와 수동 캡처는 입력 방식만 다릅니다.
   - 이미지 전처리, AI 분석, 결과 분류, 액션 제안, 오버레이 표시는 하나의 공통 파이프라인으로 처리합니다.

### Play Store 배포 기준 결정

MVP는 Google Play Store 배포를 전제로 하며, Android 공식 권한 모델 안에서 동작하도록 설계합니다.

- Shizuku 기반 무중단 캡처는 MVP 및 Play Store 빌드에서 제외합니다.
- 접근성 서비스 기반 화면 읽기는 MVP에서 제외합니다.
- 수동 화면 분석은 사용자가 명시적으로 시작한 `MediaProjection` 기반 캡처를 사용합니다.
- 자동 분석은 사용자가 직접 생성한 스크린샷 감지 또는 사용자가 직접 선택/공유한 이미지 분석으로 제한합니다.
- 모든 외부 AI 전송은 사용자 설정과 명시적 동의를 전제로 합니다.
- 실제 액션 실행은 항상 사용자 확인 후 수행합니다.

Shizuku, ADB, root, 접근성 기반 고급 기능은 향후 별도 배포판 또는 고급판 후보로만 관리합니다.

---

## 2. 통합 UX 시나리오

### 시나리오 A. 스크린샷 기반 자동 액션

1. 사용자가 다른 앱에서 화면을 보다가 스크린샷을 찍습니다.
2. 앱이 스크린샷 생성을 감지합니다.
3. 이미지를 메모리 또는 임시 스트림에서 전처리합니다.
4. AI가 화면 내용을 분석합니다.
5. 액션 가능성이 높으면 하단 오버레이 카드가 표시됩니다.
6. 사용자는 다음 중 하나를 선택합니다.
   - 일정 등록
   - 코드 복사
   - 영수증 정리
   - 메모 저장
   - TODO 생성
   - 무시

### 시나리오 B. 플로팅 버블 기반 수동 분석

1. 사용자는 화면 가장자리에 떠 있는 버블을 누릅니다.
2. 앱이 Android 시스템 캡처 승인 UI를 표시합니다.
3. 사용자가 승인하면 현재 화면을 캡처합니다.
4. 현재 활성 앱 패키지명, 화면 크기, 입력 모드 등 허용 가능한 컨텍스트 힌트를 함께 AI에 전달합니다.
5. 확장형 오버레이 패널에서 분석 결과가 표시됩니다.
6. 긴 결과는 스트리밍 방식으로 표시하고, 사용자는 복사·저장·액션 실행을 선택할 수 있습니다.

### 시나리오 C. Batch Review

1. 여러 장의 스크린샷이 연속으로 감지됩니다.
2. 앱은 각 이미지를 큐에 넣고 순차 분석합니다.
3. 즉시 팝업이 과도하게 뜨지 않도록 중요도 높은 결과만 표시합니다.
4. 나머지는 앱 내부의 “나중에 검토” 화면에서 한 번에 확인합니다.

---

## 3. 기능 범위

### 3.1 핵심 기능

| 기능 | 설명 | 호출 방식 |
|---|---|---|
| 스크린샷 자동 감지 | 사용자가 찍은 스크린샷을 감지 | 자동 |
| 플로팅 버블 | 사용자가 원할 때 현재 화면 분석 | 수동 |
| AI 이미지 분석 | 화면 내용 분류 및 구조화 | 공통 |
| 액션 카드 | 실행 가능한 결과만 간결히 제안 | 공통 |
| 스트리밍 패널 | 긴 설명·요약 결과를 실시간 표시 | 수동 중심 |
| 결과 복사 | 코드, 텍스트, 요약 결과를 클립보드에 복사 | 공통 |
| 캘린더 등록 | 일정 후보를 사용자가 선택한 캘린더에 등록 | 자동/수동 |
| 영수증 정리 | 결제 정보 추출 후 저장 또는 내보내기 | 자동/수동 |
| 메모/TODO 생성 | 화면 내용을 요약해 메모 또는 할 일로 저장 | 자동/수동 |

### 3.2 MVP 범위

MVP에서는 다음 기능만 구현합니다.

1. 플로팅 버블 표시
2. 사용자 명시 승인 기반 수동 화면 캡처
3. 스크린샷 자동 감지
4. 이미지 리사이징 및 압축
5. AI JSON 분석
6. 액션 타입 4종
   - SCHEDULE
   - CODE
   - RECEIPT
   - NOTE
7. 하단 액션 카드 표시
8. 결과 복사
9. 사용자 선택 캘린더에 일정 등록
10. 개인정보 저장 최소화

---

## 4. 기술 스택

| 영역 | 선택 |
|---|---|
| 플랫폼 | Android Native |
| 언어 | Kotlin |
| 비동기 처리 | Coroutines, Flow |
| UI | Jetpack Compose, Material Design 3 |
| 아키텍처 | MVVM + Clean Architecture 경량 구조 |
| 의존성 주입 | Hilt |
| 설정 저장 | DataStore |
| 이미지 로딩/미리보기 | Coil |
| AI 모델 | Gemini Vision 계열 또는 Claude Vision 계열 중 추상화 가능 구조 |
| JSON 파싱 | Kotlin Serialization 또는 Moshi |
| 캡처 방식 | ScreenCaptureCallback, MediaStore ContentObserver, 사용자 승인 기반 MediaProjection |

---

## 5. 전체 아키텍처

```text
[Capture Sources]
  ├─ Screenshot Detection
  │   ├─ API 34+ ScreenCaptureCallback
  │   └─ API 33- MediaStore ContentObserver
  │
  └─ Manual Capture
      ├─ Floating Bubble
      ├─ User-initiated MediaProjection
      └─ Image Picker / Share Intent Option

        ↓

[Image Pipeline]
  ├─ Decode from Uri / Stream
  ├─ Resize
  ├─ Compress
  ├─ Duplicate Check
  └─ Sensitive Data Guard

        ↓

[AI Core]
  ├─ Prompt Builder
  ├─ Context Injection
  ├─ Vision Model Client
  ├─ JSON Response Parser
  └─ Confidence Evaluator

        ↓

[Action Engine]
  ├─ ScheduleAction
  ├─ CodeCopyAction
  ├─ ReceiptAction
  ├─ NoteAction
  ├─ TodoAction
  └─ UnknownAction

        ↓

[Overlay UX]
  ├─ Floating Bubble
  ├─ Bottom Action Card
  ├─ Expanded Result Panel
  ├─ Batch Review Screen
  └─ Settings Screen
```

---

## 6. 모듈별 설계

## Module A. Overlay UI Service

### 역할
화면 위에 플로팅 버블, 하단 액션 카드, 확장형 결과 패널을 표시합니다.

### 구성 요소

1. **Floating Bubble**
   - 화면 가장자리 고정
   - 드래그 이동 가능
   - 클릭 시 현재 화면 분석
   - 길게 누르면 빠른 메뉴 표시

2. **Bottom Action Card**
   - 자동 감지 결과가 액션 가능할 때 표시
   - 사용자의 흐름을 방해하지 않도록 하단에 작게 표시
   - 일정 시간 후 자동 dismiss
   - Swipe dismiss 지원

3. **Expanded Result Panel**
   - 수동 분석 결과 표시
   - 긴 텍스트는 스트리밍 방식으로 렌더링
   - 복사, 저장, 다시 분석 버튼 제공

### 구현 포인트

- `WindowManager` 기반 오버레이 사용
- Compose View를 Service 내부에서 렌더링
- `ViewTreeLifecycleOwner`, `ViewModelStoreOwner`, `SavedStateRegistryOwner` 수동 설정
- 최소 터치 영역 48dp 이상 보장
- 상태바, 내비게이션바, 태스크바 영역 침범 방지
- Dex 모드와 태블릿 PC 모드에서 DPI 반응형 레이아웃 적용

---

## Module B. Capture Engine

### 역할
자동 또는 수동 방식으로 화면 이미지를 확보합니다.

### B-1. 자동 스크린샷 감지

#### API 34 이상
- `ScreenCaptureCallback` 사용
- OS 공식 이벤트를 우선 사용

#### API 33 이하
- `ContentObserver`로 `MediaStore.Images` 변경 감지
- 파일명, 생성 시간, 경로 패턴을 기준으로 스크린샷 후보 판단

#### 중복 방지
- 최근 처리한 `Uri`, 파일명, 생성 시각, 이미지 해시를 캐싱
- 짧은 시간 내 동일 이미지가 감지되면 1회만 처리

### B-2. 수동 화면 캡처

#### 1순위: 사용자 시작 MediaProjection
- 사용자가 플로팅 버블 또는 앱 내부 버튼을 눌렀을 때만 캡처를 시작합니다.
- Android 시스템의 MediaProjection 승인 UI를 통해 사용자가 명시적으로 허용한 경우에만 현재 화면을 캡처합니다.
- 캡처 이미지는 디스크에 저장하지 않고 메모리 스트림에서 전처리합니다.
- 분석 완료 후 원본 비트맵 참조를 즉시 해제합니다.

#### 2순위: 사용자 선택 이미지 / 공유 인텐트
- 사용자가 갤러리, 파일 선택기, 공유 메뉴를 통해 이미지를 직접 전달할 수 있도록 합니다.
- 이 경로는 권한 요구가 적고 Play Store 심사 리스크가 낮으므로 안정적인 대체 경로로 유지합니다.

#### 제외: Shizuku / 접근성 기반 화면 읽기
- Shizuku 기반 무중단 캡처는 Play Store 빌드에서 제외합니다.
- 접근성 서비스 기반 텍스트 추출은 MVP에서 제외합니다.
- 두 기능은 향후 별도 고급판 또는 사이드로드 배포판에서만 재검토합니다.

---

## Module C. Image Pipeline

### 역할
AI 전송 전에 이미지를 분석 가능한 크기와 형식으로 최적화합니다.

### 처리 순서

1. 이미지 입력 수신
2. Bitmap 디코딩
3. 긴 변 기준 1024~1536px 범위로 리사이징
4. JPEG 또는 WebP 압축
5. 중복 이미지 비교
6. 민감정보 저장 방지 처리
7. AI Core로 전달

### 보안 원칙

- 원본 이미지는 기본적으로 저장하지 않습니다.
- 디버그 로그에 이미지 경로, OCR 원문, 민감 텍스트를 남기지 않습니다.
- 임시 파일이 필요한 경우 작업 완료 즉시 삭제합니다.
- 분석 실패 시에도 원본 이미지를 보존하지 않습니다.

---

## Module D. AI Core

### 역할
이미지와 화면 컨텍스트를 AI 모델에 전달하고, 구조화된 결과를 반환합니다.

### 입력 데이터

```json
{
  "image": "compressed_image_bytes",
  "source": "SCREENSHOT_DETECTED | MANUAL_CAPTURE",
  "appPackage": "optional.package.name",
  "screenMode": "PHONE | TABLET | DEX | SPLIT_SCREEN",
  "userLocale": "ko-KR",
  "preferredActions": ["SCHEDULE", "CODE", "RECEIPT", "NOTE", "TODO"]
}
```

### 출력 JSON 스키마

```json
{
  "type": "SCHEDULE | CODE | RECEIPT | NOTE | TODO | UNKNOWN",
  "confidence": 0.0,
  "summary": "string",
  "data": {},
  "actions": [
    {
      "id": "string",
      "label": "string",
      "requiresConfirmation": true
    }
  ],
  "privacyFlags": {
    "containsSensitiveData": false,
    "sensitiveTypes": []
  }
}
```

### 타입별 데이터 구조

#### SCHEDULE

```json
{
  "title": "string",
  "date": "YYYY-MM-DD",
  "startTime": "HH:mm",
  "endTime": "HH:mm",
  "location": "string",
  "memo": "string",
  "timezone": "Asia/Seoul"
}
```

#### CODE

```json
{
  "language": "string",
  "code": "string",
  "explanation": "string",
  "copyCandidate": true
}
```

#### RECEIPT

```json
{
  "merchant": "string",
  "paidAt": "YYYY-MM-DD HH:mm",
  "amount": 0,
  "currency": "KRW",
  "paymentMethod": "string",
  "items": []
}
```

#### NOTE

```json
{
  "title": "string",
  "content": "string",
  "tags": []
}
```

#### TODO

```json
{
  "title": "string",
  "dueDate": "YYYY-MM-DD",
  "priority": "LOW | MEDIUM | HIGH",
  "memo": "string"
}
```

---

## Module E. Action Engine

### 역할
AI 분석 결과를 실제 사용자 액션으로 변환합니다.

### 공통 원칙

- 모든 액션은 기본적으로 사용자 확인 후 실행합니다.
- 신뢰도가 낮은 결과는 자동 실행하지 않습니다.
- 민감정보가 포함된 결과는 저장 전 경고를 표시합니다.
- 같은 이미지에서 같은 액션이 반복 실행되지 않도록 처리 상태를 기록합니다.

### 액션별 처리

#### 1. 일정 등록
- 사용자가 설정에서 선택한 캘린더에 등록
- 특정 개인 캘린더명은 설계서에 고정하지 않음
- 캘린더가 선택되지 않은 경우 첫 실행 시 선택 요청
- 제목, 날짜, 시간 기준으로 중복 일정 후보 확인

#### 2. 코드 복사
- 코드 영역만 추출
- UI 버튼, 상태바, 광고 문구 등 불필요한 텍스트 제거
- 언어를 추정하고 코드 블록 형태로 제공
- 사용자 확인 후 클립보드 복사

#### 3. 영수증 정리
- 상호명, 금액, 결제일, 결제수단, 품목 추출
- 저장 전 사용자가 금액과 날짜를 확인
- MVP에서는 로컬 목록 저장 또는 CSV 내보내기까지만 제공

#### 4. 메모 저장
- 화면 내용을 제목과 본문으로 요약
- 외부 메모 앱 연동은 확장 기능으로 분리
- MVP에서는 앱 내부 저장소에 저장

#### 5. TODO 생성
- 화면에서 할 일 후보 추출
- 마감일이 없으면 빈 값으로 유지
- 사용자 확인 후 내부 TODO 목록 또는 외부 앱 공유 인텐트로 전달

---

## Module F. Settings & Permission Manager

### 권한 목록

| 권한 | 목적 | 요청 시점 |
|---|---|---|
| SYSTEM_ALERT_WINDOW | 오버레이 표시 | 플로팅 버블 활성화 시 |
| POST_NOTIFICATIONS | 포그라운드 서비스 알림 | Android 13+에서 서비스 활성화 시 |
| READ_MEDIA_IMAGES | 스크린샷 이미지 접근 | Android 13+ 자동 감지 활성화 시 |
| READ_EXTERNAL_STORAGE | 하위 버전 이미지 접근 | Android 12 이하 자동 감지 활성화 시 |
| MediaProjection 승인 | 현재 화면 수동 캡처 | 사용자가 수동 분석을 시작할 때 |
| Calendar 권한 | 일정 등록 | 일정 등록 기능 사용 시 |

### 권한 UX 원칙

- 앱 첫 실행 시 모든 권한을 한 번에 요청하지 않습니다.
- 사용자가 켜는 기능에 필요한 권한만 단계적으로 요청합니다.
- 권한 거부 시 대체 기능 또는 제한 사항을 명확히 안내합니다.

### Play Store 빌드 제외 기능

다음 기능은 MVP 및 Play Store 배포판에 포함하지 않습니다.

- Shizuku 권한 기반 캡처
- ADB/root 기반 캡처
- 사용자가 직접 시작하지 않은 무중단 화면 캡처
- 접근성 서비스 기반 일반 화면 텍스트 수집
- 사용자의 확인 없는 자동 액션 실행

해당 기능은 향후 별도 패키지명, 별도 배포 채널, 별도 개인정보 고지 체계로 분리하는 것을 원칙으로 합니다.

---

## 7. 개인정보 제거 및 보호 설계

### 설계서에서 제거/치환한 항목

| 기존 항목 유형 | 처리 방식 |
|---|---|
| 특정 캘린더명 | “사용자 선택 캘린더”로 치환 |
| 특정 개인/그룹명으로 보일 수 있는 명칭 | 범용 기능명으로 치환 |
| 특정 사용 목적이 과도하게 개인화된 문장 | 범용 생산성 시나리오로 재작성 |
| 민감정보 예시 | 일반 보안 정책으로만 유지 |

### 앱 내 개인정보 보호 원칙

1. 사용자가 기능을 켠 경우에만 화면 감지 또는 캡처를 수행합니다.
2. 원본 이미지는 기본 저장하지 않습니다.
3. AI 분석 로그에는 원문 텍스트와 원본 이미지를 남기지 않습니다.
4. 주민등록번호, 계좌번호, 카드번호, 인증번호 등은 저장하지 않습니다.
5. 민감정보가 감지된 결과는 액션 실행 전 사용자에게 경고합니다.
6. AI 전송 여부를 설정에서 끌 수 있도록 합니다.
7. 가능하면 온디바이스 OCR을 먼저 사용하고, 필요한 경우에만 클라우드 AI를 호출합니다.

---

## 8. 에러 처리

### AI 분석 실패
- 이미지 품질이 낮거나 텍스트가 적으면 `UNKNOWN` 처리
- 반복 팝업 대신 조용한 실패 처리
- 수동 분석인 경우 오버레이 내부에 재시도 버튼 표시

### 네트워크 오류
- Exponential Backoff 방식으로 제한적 재시도
- 장시간 실패 시 큐에 보관하지 않고 사용자에게 상태 안내
- API 할당량 초과 시 다음 행동을 안내

### MediaProjection 승인 거부 또는 중단
- 사용자가 화면 캡처 승인을 거부하면 분석을 시작하지 않습니다.
- 오버레이 내부에 “화면 캡처 권한이 필요합니다” 상태를 표시합니다.
- 대체 경로로 사용자가 직접 이미지를 선택하거나 공유할 수 있게 안내합니다.
- 사용자가 거부한 권한을 반복적으로 강요하지 않습니다.

### 캘린더 등록 실패
- 캘린더 권한 확인
- 사용자 선택 캘린더 존재 여부 확인
- 실패 시 일정 데이터를 클립보드 또는 공유 인텐트로 내보낼 수 있게 제공

---

## 9. 데이터 저장 정책

### 저장하는 데이터

- 사용자 설정
- 선택한 캘린더 ID
- 오버레이 위치
- 최근 처리 이미지의 중복 방지용 해시
- 사용자가 명시적으로 저장한 메모/TODO/영수증 결과

### 저장하지 않는 데이터

- 원본 스크린샷 이미지
- 전체 OCR 원문 로그
- API Key 원문
- 카드번호, 계좌번호, 주민등록번호, 인증번호 등 민감정보

### API Key 관리

- 앱 코드에 API Key 하드코딩 금지
- 개발 환경에서는 `local.properties` + `BuildConfig` 사용
- 배포 환경에서는 서버 프록시 또는 원격 설정 구조 권장
- Git 저장소에 키가 포함되지 않도록 `.gitignore` 구성

---

## 10. 구현 마일스톤

### Phase 1. 프로젝트 기반 구성
- Kotlin Android 프로젝트 생성
- Compose + Material 3 설정
- Hilt 설정
- DataStore 설정
- MVVM 기본 구조 구성

### Phase 2. Overlay MVP
- Foreground Service 구현
- WindowManager 기반 Compose Overlay 구현
- 플로팅 버블 표시
- 드래그 이동 및 화면 경계 처리
- 오버레이 권한 요청 Flow 구현

### Phase 3. Manual Capture MVP
- 사용자 시작 MediaProjection 구현
- 시스템 화면 캡처 승인 Flow 구현
- 이미지 선택기 및 공유 인텐트 입력 경로 구현
- 캡처 이미지 메모리 처리
- 이미지 리사이징/압축 구현

### Phase 4. Screenshot Detection
- API 34+ ScreenCaptureCallback 구현
- API 33 이하 ContentObserver 구현
- MediaStore 이미지 접근 권한 처리
- 중복 감지 방지 캐시 구현

### Phase 5. AI Core
- Prompt Builder 구현
- 모델 클라이언트 추상화
- JSON 응답 강제
- 스키마 파싱 및 검증
- confidence 기준 액션 분기

### Phase 6. Action Card
- 하단 액션 카드 UI 구현
- 자동 dismiss 구현
- Swipe dismiss 구현
- 액션별 확인 Dialog 구현

### Phase 7. 액션 기능 구현
- 일정 등록
- 코드 복사
- 영수증 정리
- 메모 저장
- TODO 생성

### Phase 8. 안정화
- 권한 거부 케이스 정리
- 네트워크 오류 처리
- 배터리 사용량 점검
- 로그 내 개인정보 제거 점검
- Dex/태블릿/분할 화면 UI 점검

---

## 11. AI 에이전트 코딩 컨벤션

AI 코딩 에이전트가 코드를 작성할 때 주요 클래스와 함수 상단에 다음 주석 형식을 사용합니다.

```kotlin
/*
 * Input: [파라미터 명 및 타입]
 * Output: [반환 데이터 구조 - Result<T> 또는 Flow<T>]
 * 핵심 로직: [안드로이드 시스템 API 호출 순서 및 스레드 전환 방식]
 * 이 로직을 작성한 이유: [왜 이 방식이 필요한지 설명]
 */
```

### 네이밍 규칙
- Kotlin 표준 스타일 사용
- 변수/함수: camelCase
- 클래스/인터페이스: PascalCase
- C# 스타일 네이밍 금지

### 금지 사항
- API Key 하드코딩 금지
- `lateinit` 남용 금지
- 원본 이미지 로그 저장 금지
- 권한 없는 백그라운드 캡처 시도 금지
- 사용자가 끈 기능을 백그라운드에서 계속 실행 금지

---

## 12. 배포 전략

### Play Store 버전

Play Store 버전은 공식 Android API와 사용자 명시 동작만 사용합니다.

- 수동 분석: MediaProjection 승인 후 1회성 캡처
- 자동 분석: 사용자가 직접 생성한 스크린샷 감지
- 대체 입력: 이미지 선택기, 공유 인텐트
- 액션 실행: 사용자 확인 후 실행
- 저장 정책: 원본 이미지와 전체 OCR 원문 저장 금지

### 향후 고급판 후보

Play Store 정책 리스크가 큰 기능은 별도 고급판 후보로 분리합니다.

- Shizuku 기반 빠른 캡처
- ADB/root 기반 고급 자동화
- 로컬 전용 분석 모드
- 외부 배포 채널 전용 기능

고급판을 만들 경우 Play Store 버전과 패키지명, 권한 설명, 개인정보 고지, 기능 설명을 분리합니다.

---

## 13. 최종 통합 방향

이 앱은 “스크린샷 자동 액션 앱”과 “수동 화면 분석 오버레이 앱”을 억지로 합친 구조가 아니라, **입력 방식만 다른 하나의 Context-to-Action 플랫폼**으로 설계합니다.

핵심은 다음 세 가지입니다.

1. **Capture는 여러 방식으로 받는다.**
   - 스크린샷 자동 감지
   - 플로팅 버블 수동 캡처
   - 사용자 승인 기반 MediaProjection
   - 사용자 선택 이미지 / 공유 인텐트

2. **분석은 하나의 AI Core로 통합한다.**
   - 이미지 전처리
   - 컨텍스트 힌트 주입
   - JSON 구조화
   - confidence 평가

3. **결과는 액션 중심으로 제공한다.**
   - 설명보다 실행 가능한 제안을 우선
   - 사용자의 흐름을 방해하지 않는 오버레이
   - 개인정보 저장 최소화


---

## 14. 결정 로그

### 2026-04-28: Play Store 배포 우선 결정

- MVP 기준을 Play Store 배포 가능성 중심으로 변경합니다.
- Shizuku 기반 무중단 캡처는 MVP에서 제외합니다.
- 수동 분석은 사용자 승인 기반 MediaProjection으로 구현합니다.
- 자동 분석은 사용자가 직접 찍은 스크린샷 감지 중심으로 구현합니다.
- 접근성 서비스 기반 화면 읽기는 MVP에서 제외합니다.
- 향후 필요 시 Shizuku 기능은 별도 고급판 또는 외부 배포판으로 검토합니다.

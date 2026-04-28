# Context Action Assistant

설계서(`Unified_Context_Action_Assistant_PlayStore_Spec_v1_1.md`) 기반 Android Native MVP 스캐폴딩입니다.

## 포함된 범위
- Compose + Material3 + Hilt + DataStore 기반 프로젝트 구조
- `OverlayService` 기반 플로팅 버블 및 하단 액션 카드
- `ContentObserver` 기반 스크린샷 감지(MVP 경로)
- 이미지 전처리(리사이즈/압축/SHA-256)
- AI Core 추상화(`VisionAnalyzer`) + Mock 구현
- 액션 엔진(요약 복사 / 캘린더 등록 인텐트)

## 현재 상태
- 설계서의 Phase 1~4 중심으로 동작 골격을 우선 구현
- MediaProjection 1회 실캡처 승인 플로우 연결
- Gemini Vision API 연동(키 미설정/실패 시 Mock fallback)

## 주요 패키지
- `ai`: AI 분석 인터페이스/구현
- `capture`: 스크린샷 감지
- `pipeline`: 이미지 처리
- `action`: 액션 실행
- `overlay`: 오버레이 서비스 UI
- `data/datastore`: 사용자 설정 저장

## 다음 작업 권장
1. Batch Review 화면 및 결과 저장 화면 추가
2. TODO 액션/영수증 CSV 내보내기/캘린더 중복검사 정밀화
3. 런타임 권한 UX 세분화 및 거부 시나리오 가이드 강화
4. 테스트 코드 및 성능/배터리 점검

## API 키 설정
`local.properties`:
```properties
GEMINI_API_KEY=your_key_here
```

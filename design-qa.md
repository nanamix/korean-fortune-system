# 디자인 QA

## 검증 기준

- 시각 기준안: `/Users/jyha/Library/Application Support/orca/codex-accounts/82a88e2a-4341-4b83-b178-73ab8f2ca59f/home/generated_images/019fbd5e-bc7a-7191-9ff9-d0644ca41de1/exec-74178744-8793-4372-a771-95cda50ee00b.png`
- 구현 화면: `/tmp/korean-fortune-daily-v31.png`
- 구현 콘텐츠 정규화본: `/tmp/korean-fortune-daily-content.png`
- 동일 화면 비교본: `/tmp/korean-fortune-design-comparison.png`
- 랜딩 데스크톱: `/tmp/korean-fortune-landing-final.png`
- 랜딩 모바일: `/tmp/korean-fortune-landing-mobile-final.png`
- 앱 모바일: `/tmp/korean-fortune-app-mobile.png`
- AI 상담: `/tmp/korean-fortune-ai-final.png`

## 환경과 상태

- 브라우저: Safari
- 데스크톱 창: 1264 × 870 CSS px
- 모바일 창: 430 × 850 CSS px
- 기준안 원본: 1487 × 1058 px
- 구현 스크린샷 원본: 2664 × 1876 px(브라우저 크롬과 그림자 포함)
- 정규화: 브라우저 크롬을 제외한 콘텐츠를 자른 뒤 기준안과 같은 1058 px 높이로 맞춰 한 화면에서 비교
- 검증 상태: 비로그인 로컬 정적 미리보기, `daily`, `saju`, `ai` 탭

## 비교 결과

- 전체 화면: 한지 아이보리 배경, 먹색 명조 타이포그래피, 주홍색 CTA, 금빛 보조색, 산수화와 만세력 원형 모티프가 기준안의 브랜드 톤과 일치한다.
- 정보 구조: `오늘`을 기본 홈으로 두고 날짜, 오늘의 메시지, 주 CTA, 일·관계·마음 가이드를 첫 화면에 배치했다.
- 랜딩: 기능 목록과 개발자 링크 대신 가치 제안, 개인정보 신뢰 문구, 해석 방법, 결과 미리보기를 우선한다.
- 앱 이동: `오늘 → 깊이 보기 → AI 상담 → 내 정보`로 소비자 언어를 사용하며 기존 계산 기능은 하위 화면에서 유지한다.
- 반응형: 모바일에서 히어로 가독성과 CTA 크기를 유지하며 랜딩의 네 가지 핵심 경로를 고정 하단 내비게이션으로 제공한다.

## 발견 사항과 수정 이력

1. P1 — 모바일 랜딩에서 중간 내비게이션 두 항목이 숨겨짐.
   - 수정: 네 항목을 항상 노출하는 4열 고정 하단 내비게이션으로 교체.
   - 재검증: 430 × 850 CSS px에서 `오늘의 운세`, `깊이 보기`, `AI 상담`, `내 정보` 모두 표시됨.
2. P1 — AI 상담에서 외부 제공자 기술 상태가 사용자 질문보다 먼저 노출됨.
   - 수정: 가치 중심 안내와 질문 입력을 먼저 배치하고 제공자 상태는 접힌 `details`로 이동.
   - 재검증: 첫 화면에서 질문 예시, 입력, 생성 CTA가 우선 노출됨.
3. P3 — 기준안의 장식 아이콘과 하단 보관함 유도는 구현에서 단순화됨.
   - 판단: 핵심 시각 언어와 기존 기능 경로는 유지되며, 새 보관 기능을 발명하지 않기 위한 의도적 범위 차이.

## 상호작용 및 회귀 검증

- 모바일 메뉴 버튼: `aria-expanded=true` 전환 확인.
- 주요 탭 클릭: `saju|#saju|page`, `ai|#ai|page`, `daily|#daily|page`로 상태, URL 해시, `aria-current` 동기화 확인.
- 정적 미리보기의 API 404는 백엔드 없는 시각 검증 환경의 예상 결과이며, UI가 기술 상태를 주 흐름보다 앞세우지 않도록 처리함.
- `git diff --check`: 통과.
- `./gradlew test`: `BUILD SUCCESSFUL`(전체 테스트 통과, 문서 접근성 테스트 1건은 기존 조건에 따라 SKIPPED).

## 최종 결과

passed

# 14. 서양 점성술 계산

> 별자리 운세의 열대황도 출생 차트, 대상일 transit, 점수 산식과 정확도 경계를 설명합니다.

## 14.1 계산 범위

`WesternAstrologyService`의 `tropical-approx-v1` 모델은 다음 값을 결정론적으로 계산합니다.

- 출생 태양과 달의 황경, 태양궁·달궁 및 별자리 내 각도
- 출생 시각·위도·경도가 있을 때 상승궁
- 태양궁의 원소, 양상, 전통·현대 주인 행성, 데칸
- 태양과 달의 각거리로 구한 출생 달 위상
- 대상일 정오의 이동 태양·달과 출생 태양·달·상승궁 사이 주요 각

황경은 춘분점을 0°로 하는 열대황도에서 30°씩 12별자리에 배정합니다. Julian date와 항성시의 기본 개념은 [USNO Julian Date](https://aa.usno.navy.mil/faq/JD_formula), [USNO sidereal time](https://aa.usno.navy.mil/faq/GAST)을 참고했습니다. 정밀 천체력의 대표 구현과 차이는 [Astrodienst Swiss Ephemeris](https://www.astro.com/swisseph/index.htm)를 경계 기준으로 삼습니다.

## 14.2 입력과 정밀도

| 입력 상태 | `precision` | 계산 결과 |
|---|---|---|
| 출생일만 제공 | `DATE_ONLY` | 현지 정오 기준 태양궁·달궁 |
| 출생일·시각·위도·경도 제공 | `BIRTH_TIME_LOCATION` | 태양궁·달궁·상승궁 |

`timeZone`은 IANA 이름을 받으며 미입력 시 `Asia/Seoul`입니다. 음력 생일은 `LunarSolarConverter`로 양력 변환 후 계산합니다. 웹 UI의 `내 정보 → 공통 사용자 정보`는 서울을 기본 출생 도시로 제공하며, 도시·지역·우편번호 검색 결과를 선택하면 Open-Meteo/GeoNames가 반환한 출생 도시·WGS84 좌표·IANA 시간대를 공통 프로필에 함께 적용합니다. 별자리 운세는 이 공통 값을 자동 사용하며 외부 검색 실패 시 수동 입력을 그대로 사용할 수 있습니다.

위치 검색어는 로그와 DB에 저장하지 않지만 좌표·시간대 조회를 위해 외부
Open-Meteo 서비스로 전송됩니다. 상세 번지 주소나 개인 식별 가능 장소 대신
출생 도시·지역·우편번호를 사용합니다.

## 14.3 위치와 주요 각

- 태양 황경: 평균 황경·평균 근점각과 주요 중심차 항을 사용한 저차 근사
- 달 황경: 평균 황경과 주요 주기항을 사용한 저차 근사
- 상승궁: UTC Julian date에서 구한 GMST에 출생 경도를 더한 local sidereal time, 황도 경사각과 위도를 사용
- 주요 각: 합 0°, 육합 60°, 사각 90°, 삼합 120°, 충 180°
- orb: 이동 태양은 6°, 이동 달은 8°

화면의 `majorTransits`와 점수 계산은 같은 orb를 사용합니다. 따라서 점수에 반영되는 각은 결과 화면에서 함께 확인할 수 있습니다.

## 14.4 점수 산식

일일 분야별 점수는 다음 내부 산식으로 계산하며 0~100 범위로 제한합니다.

```text
분야 점수 = clamp(60 + 출생 차트 조정 + transit 조정 + 날짜 리듬, 0, 100)
```

- 출생 차트 조정: 태양궁의 원소·양상을 관계, 일·성취, 건강, 재정에 매핑
- transit 조정: 주요 각의 기본값과 orb 거리에 따른 감쇠를 합산
- 날짜 리듬: 같은 입력의 재현성을 위한 대상일 기반 -4~+4 내부 조정
- 종합 점수: 네 분야 점수의 산술평균을 반올림

월간 점수도 60점에서 출생 차트 평균, 대상일 transit 평균, 대상 연월 기반 날짜 리듬을 더합니다. 이 점수는 서비스의 설명 가능한 오락용 지표이며 통계적 예측 확률이나 천문학적 영향도를 의미하지 않습니다.

## 14.5 AI 서술 경계와 개인정보

결정론 엔진이 계산한 차트·transit·점수만 `fortune-fact-packet/v1`에 넣습니다. 외부 AI는 이를 읽기 쉬운 조언으로 서술할 뿐 행성 위치·하우스·각을 재계산하거나 새로 만들 수 없습니다.

외부 AI payload에는 이름, 원본 생년월일시, 출생 좌표, 시간대, 성별, 역법, 알림 대상이 포함되지 않습니다. 엔진 계약은 `lunar-java-1.7.4+fortune-rules-v5`, 캐시 namespace는 `fact-v1-engine-v5`입니다.

## 14.6 정확도와 사용 제한

이 모델은 태양·달과 상승궁만 다루며 행성 전체, 하우스 체계, 세차·장동·시차의 정밀 보정, 고정밀 천체력 파일은 포함하지 않습니다. 경계 시각에 가까운 별자리·상승궁은 Swiss Ephemeris 같은 전문 계산과 다를 수 있습니다.

운세와 점수는 문화·오락용 참고 정보입니다. 의료·법률·금융 또는 안전과 관련된 결정을 대신하지 않습니다.

## 14.7 Swiss Ephemeris golden fixture

`src/test/resources/fixtures/western-astrology-swiss-golden-v1.json`은
공식 Swiss Ephemeris `v2.10.3final`
(`af9823fe7b06ffefe3d3968fdc5680be8b5eec5f`)의 기준값을 저장합니다.
기준값은 공식 `libswe.a`에 연결한 최소 C probe에서 다음 API로 생성했습니다.

```text
swe_julday
swe_calc_ut(SE_SUN / SE_MOON, SEFLG_SWIEPH | SEFLG_SPEED)
swe_houses(UTC Julian day, latitude, east-positive longitude, 'P'), ascmc[0]
```

fixture는 서울 개인화 사례, J2000, 춘분 0° 경계, 남반구 시드니, 1948년
역사 시점, 고위도 레이캬비크 6건을 포함합니다.
`WesternAstrologySwissGoldenFixtureTest`는 0°/360° 경계를 고려한 원형 각도
차이와 별자리 구간을 검증합니다.

| 위치 | 허용 오차 | 6건 최대 실측 편차 |
|---|---:|---:|
| Sun | 0.02° | 0.014562° |
| Moon | 0.15° | 0.084775° |
| Rising | 0.05° | 0.010067° |

춘분처럼 기준값이 별자리 경계에서 허용 오차보다 가까우면 황경 오차만
검증하고 별자리 enum 일치는 강제하지 않습니다. 그 밖의 fixture는 황경과
별자리 구간을 함께 고정합니다.

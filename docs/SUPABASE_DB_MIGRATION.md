# Supabase DB-only migration

## 범위

- Supabase PostgreSQL을 애플리케이션 영속 DB로 사용한다.
- Spring Boot/JPA/Flyway, Cloudflare Access, OpenBao, Discord·Telegram 알림 구조는 유지한다.
- Supabase Auth, Data API, Storage, Realtime 도입은 이번 이관 범위가 아니다.

Flyway로 생성하는 `public` 테이블은 모두 RLS를 활성화하고 정책을 만들지 않는다.
따라서 `anon`/`authenticated` Data API 접근은 기본 차단되며, 서버의 직접 PostgreSQL
연결만 사용한다.

## 연결 계약

`supabase` 프로필은 다음 OpenBao key가 모두 있어야 시작한다.

- `SUPABASE_DB_URL`: `jdbc:postgresql://...:5432/postgres?sslmode=require`
- `SUPABASE_DB_USER`
- `SUPABASE_DB_PASSWORD`
- 선택: `SUPABASE_DB_POOL_SIZE` (기본 5), `SUPABASE_DB_MIN_IDLE` (기본 1)

장기 실행 Docker host가 direct endpoint의 IPv6에 접근할 수 없으면 Supavisor
session pooler(5432)를 사용한다. transaction pooler(6543)는 Flyway와 데이터 이관
연결로 사용하지 않는다.

## 사전 검증

1. `./gradlew test`와 `./gradlew bootJar`를 통과시킨다.
2. Docker가 있는 환경에서 `SupabasePostgresMigrationTest`가 skip 없이 통과하는지 확인한다.
3. 원본 MySQL의 테이블별 행 수, 최대 ID, DB 크기와 쓰기량을 기록한다.
4. Supabase 비운영 프로젝트에서 Flyway V1~V2와 Hibernate `validate`를 확인한다.
5. 모든 애플리케이션 테이블에서 RLS가 활성화되고 Data API 정책이 없는지 확인한다.
6. 실제 자격증명은 OpenBao에만 등록하고 로그·명령행·Git에 남기지 않는다.

## 리허설 이관

Supabase 공식 MySQL migration 도구 또는 `pgloader`를 사용하되 먼저 비운영
프로젝트에서 수행한다. Flyway가 스키마의 source of truth이므로 대상 스키마를 먼저
생성하고 데이터만 적재한다. 다음 테이블은 최소 검증 대상이다.

- `users`, `user_roles`, `saju_data`
- `security_audit_log`
- `tojeong_gwa`
- `notification_schedule`

MySQL의 identity 값이 보존된 뒤 PostgreSQL identity sequence를 각 테이블의
`MAX(id)` 다음 값으로 맞춘다. 외래키 위반, NULL/enum 변환, JSON 유효성, UTF-8 한글,
예약 시간과 `Asia/Seoul` 해석을 별도로 확인한다.

## 운영 cutover gate

운영 변경은 별도 승인 후 다음 순서로 진행한다.

1. 기존 MySQL 백업과 복구 시험 결과를 확보한다.
2. 알림 예약 실행을 일시 중지하고 애플리케이션 쓰기를 차단한다.
3. 마지막 증분 데이터를 적재하고 테이블별 행 수·핵심 샘플·sequence를 대조한다.
4. OpenBao에 Supabase key를 등록하고 런타임 프로필을 `supabase`로 전환한다.
5. Flyway version, Hibernate validate, Actuator health, 로그인, 운세 조회, 예약 CRUD를 확인한다.
6. 테스트 수신처로만 예약 발송을 확인한다.

## rollback

- cutover 전 MySQL을 삭제하거나 volume을 정리하지 않는다.
- rollback 시 애플리케이션 쓰기를 다시 차단한 뒤 MySQL 프로필과 기존 secret을 복원한다.
- Supabase에서 발생한 신규 쓰기가 있으면 데이터 손실 범위를 먼저 계산한다. 양쪽 DB를
  동시에 쓰기 가능 상태로 두지 않는다.
- rollback 검증이 끝날 때까지 MySQL은 read-only 보존 대상으로 취급한다.

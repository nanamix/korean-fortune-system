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

운영 cutover 이후 Compose 기본 프로필은 `prod,supabase`다. 장애 rollback에서만
`SPRING_PROFILES_ACTIVE=prod`로 덮어써 기존 MySQL로 되돌린다.

```bash
SPRING_PROFILES_ACTIVE=prod,supabase
OPENBAO_REQUIRED_KEYS="MYSQL_ROOT_PASSWORD MYSQL_PASSWORD GRAFANA_PASSWORD JWT_SECRET CF_ACCESS_TEAM_DOMAIN CF_ACCESS_AUD SUPABASE_DB_URL SUPABASE_DB_USER SUPABASE_DB_PASSWORD"
```

MySQL 관련 key와 컨테이너는 rollback 검증이 끝날 때까지 유지한다. 위 변수에 실제
자격증명 값을 넣지 않으며, `SUPABASE_DB_*` 값은 OpenBao secret에서만 공급한다.

## 사전 검증

1. `./gradlew test`와 `./gradlew bootJar`를 통과시킨다.
2. Docker가 있는 환경에서 `SupabasePostgresMigrationTest`가 skip 없이 통과하는지 확인한다.
3. 원본 MySQL의 테이블별 행 수, 최대 ID, DB 크기와 쓰기량을 기록한다.
4. Supabase 비운영 프로젝트에서 Flyway V1~V2와 Hibernate `validate`를 확인한다.
5. 모든 애플리케이션 테이블에서 RLS가 활성화되고 Data API 정책이 없는지 확인한다.
6. 실제 자격증명은 OpenBao에만 등록하고 로그·명령행·Git에 남기지 않는다.

## 2026-08-01 스키마 리허설 실행 기록

### 대상과 연결 확인

- Supabase project ref: `kelknpbvpiabmytayxrq`
- Supabase Dashboard branch: `main` (`PRODUCTION` 표시)
- region: `ap-northeast-2`
- 연결 방식: Supavisor shared pooler session mode, port `5432`
- host: `aws-1-ap-northeast-2.pooler.supabase.com`
- database: `postgres`
- user: `postgres.kelknpbvpiabmytayxrq`
- 애플리케이션 JDBC URL은 `sslmode=require`를 사용한다.
- 실제 비밀번호는 1Password의 `supabase korean-fortune-system` 항목에서
  `SUPABASE_DB_PASSWORD` 비밀번호 필드로 관리하며 문서·Git·채팅에 기록하지 않는다.

Supabase MCP로 프로젝트 region을 확인했다. MCP 응답에는 pooler host와 user가 포함되지
않아 Supabase Dashboard의 `Connect > Direct Connection string > Session pooler` 화면에서
정확한 연결 값을 읽기 전용으로 교차 확인했다. 운영 애플리케이션 프로필 전환이나
OpenBao 반영은 수행하지 않았다.

### 사전 게이트 결과

- `./gradlew test bootJar`: 성공
- `./gradlew test --tests com.fortune.config.SupabasePostgresMigrationTest --rerun-tasks`:
  Docker PostgreSQL에서 Flyway V1~V2, Hibernate `validate`, RLS 검증 성공
- direct endpoint는 현재 실행 환경의 IPv6 경로로 연결할 수 없어 IPv4 Session Pooler를
  사용했다.
- 최초 두 번의 실제 연결은 잘못된 Database Password로 `SQLSTATE 28P01`이 발생했으며,
  Flyway 실행 전에 실패해 부분 스키마 변경은 없었다.
- 1Password 필드 수정 후 Session Pooler에서 읽기 전용 `SELECT 1`로 인증 성공을 확인한
  다음 Flyway를 실행했다.

### Supabase 적용·검증 결과

- 대상 PostgreSQL: `17.6`
- Flyway V1 `create core schema`: 성공
- Flyway V2 `create notification schedule`: 성공
- 현재 Flyway version: `2`
- Spring Boot `supabase` 프로필과 Hibernate `ddl-auto=validate`: 성공
- 검증 후 리허설 애플리케이션과 Hikari pool: 정상 종료

다음 6개 애플리케이션 테이블의 존재와 RLS 활성화를 읽기 전용 쿼리로 확인했다.

- `users`
- `user_roles`
- `saju_data`
- `security_audit_log`
- `tojeong_gwa`
- `notification_schedule`

위 테이블의 `pg_policies` 개수는 `0`이다. 따라서 현재 상태는 서버의 PostgreSQL 직접
연결만 허용하고 `anon`/`authenticated` Data API 접근은 기본 차단하는 DB-only 계약과
일치한다.

### 현재 상태와 다음 순서

이 시점에 완료한 범위는 Supabase 연결 확인과 빈 `public` 스키마에 대한 Flyway 스키마
리허설까지였다. 원본 MySQL 데이터 적재는 아래 데이터 적재 리허설 기록에서 이어서
완료했다. OpenBao secret 반영, 운영 `SPRING_PROFILES_ACTIVE=prod,supabase` 전환과 배포는
아직 수행하지 않았다.

1. 원본 MySQL의 테이블별 행 수, 최대 identity, DB 크기와 최근 쓰기량을 읽기 전용으로
   수집한다.
2. MySQL과 PostgreSQL 타입·NULL·enum·JSON·시간대 변환 규칙을 확정한다.
3. 대상 테이블을 비우거나 덮어쓰지 않는 데이터 전용 리허설 절차와 rollback을 검토한다.
4. 별도 승인 후 데이터 적재 리허설을 수행하고 행 수, 핵심 샘플, FK, sequence를 대조한다.
5. 운영 cutover는 아래 gate를 모두 충족한 뒤 별도로 승인받는다.

## 2026-08-01 원본 MySQL 읽기 전용 인벤토리

### 원본 상태

- 운영 host: `nanamix2019.local`
- container: `korean-fortune-mysql` (`mysql:8.0.35`, healthy)
- database: `korean_fortune`
- session time zone: `+09:00`
- `read_only`: `0`이므로 운영 애플리케이션이 쓸 수 있는 상태다.
- `information_schema` 기준 전체 data+index 크기: `425984` bytes (`416 KiB`)
- OpenBao의 `MYSQL_PASSWORD_FILE`을 컨테이너 안에서만 소비했으며 값을 출력하거나
  로컬 파일로 복사하지 않았다.

정확한 `COUNT(*)`, `MAX(id)`, data/index 크기는 다음과 같다.

| table | rows | max id | data bytes | index bytes | next auto increment |
| --- | ---: | ---: | ---: | ---: | ---: |
| `flyway_schema_history` | 2 | N/A | 16384 | 0 | N/A |
| `fortune_cache` | 0 | NULL | 16384 | 49152 | NULL |
| `notification_schedule` | 1 | 1 | 16384 | 32768 | 2 |
| `saju_data` | 0 | NULL | 16384 | 16384 | NULL |
| `security_audit_log` | 4 | 4 | 16384 | 49152 | 5 |
| `sinsal_master` | 0 | NULL | 16384 | 0 | NULL |
| `tojeong_gwa` | 0 | NULL | 16384 | 16384 | NULL |
| `user_roles` | 0 | N/A | 16384 | 0 | NULL |
| `users` | 0 | NULL | 16384 | 114688 | NULL |

`fortune_user`에는 `performance_schema.table_io_waits_summary_by_table` 조회 권한이 없어
테이블별 최근 쓰기량은 수집하지 못했다. 권한을 변경하지 않고 전용 MySQL 컨테이너의
전역 `Com_insert`, `Com_update`, `Com_delete`, `Com_replace`를 30초 간격으로 측정했으며
모두 변화량 `0`이었다. 짧은 표본이므로 운영 cutover의 쓰기 동결을 생략하는 근거로
사용하지 않는다.

### 대상 PostgreSQL 기준선

Supabase의 `users`, `user_roles`, `saju_data`, `security_audit_log`, `tojeong_gwa`,
`notification_schedule`은 모두 `0`행이다. 다섯 identity sequence의 `last_value`도
모두 `NULL`로 아직 사용되지 않았다. 따라서 데이터 리허설은 기존 업무 데이터를
덮어쓰지 않고 빈 대상에 명시적 ID를 보존해 적재할 수 있다.

### 스키마 차이와 이관 판정

- MySQL `TINYINT(1)`은 PostgreSQL `BOOLEAN`으로 `0=false`, `1=true` 변환한다.
- MySQL `ENUM`은 현재 PostgreSQL/JPA 계약의 `VARCHAR` 문자열로 옮긴다.
- MySQL `JSON`은 JSON 유효성을 유지한 채 PostgreSQL `JSON`으로 옮긴다.
- MySQL `AUTO_INCREMENT` ID는 PostgreSQL `GENERATED BY DEFAULT AS IDENTITY`에
  명시적으로 보존하고, 적재 후 sequence를 `MAX(id)`에 맞춘다.
- MySQL `TIMESTAMP`/`DATETIME`은 PostgreSQL `TIMESTAMP WITHOUT TIME ZONE`으로 옮긴다.
  원본 세션과 애플리케이션 계약이 `Asia/Seoul`이므로 wall-clock 값을 그대로 보존하고
  별도 UTC 변환을 하지 않는다.
- MySQL `users.updated_at`의 `ON UPDATE CURRENT_TIMESTAMP`는 PostgreSQL DDL에 없지만
  JPA `@UpdateTimestamp`가 런타임 갱신을 담당한다.
- 원본에서 더 느슨한 NULL 계약은 현재 비어 있는 테이블에만 영향을 준다. 데이터가 있는
  `security_audit_log.timestamp` 4건은 모두 non-NULL이며,
  `notification_schedule` 1건도 대상의 필수 컬럼과 boolean 범위를 충족한다.
- 집계 검증 결과 source FK orphan은 `security_audit_log`, `saju_data`, `user_roles` 모두
  `0`건이다. 행 내용과 개인정보는 출력하지 않았다.

MySQL에만 있는 `fortune_cache`와 `sinsal_master`는 모두 `0`행이며 현재 JPA entity나
repository가 없다. `fortune_cache` 대신 Spring Cache/Caffeine 또는 Redis를 사용하고,
신살 규칙은 `SinsalService`의 결정론적 인메모리 테이블을 사용한다. 두 legacy 테이블은
이번 DB-only 데이터 이관 대상에서 제외한다. MySQL의 `flyway_schema_history`도 대상
PostgreSQL의 독립적인 Flyway 이력으로 대체되므로 데이터 이관하지 않는다.

### 데이터 적재 리허설 계획

현재 실제 payload는 `security_audit_log` 4행과 `notification_schedule` 1행이다. 나머지
대상 테이블은 0행이므로 payload가 없다.

1. 원본 MySQL을 계속 online 상태로 두되 적재 직전 exact count와 전역 쓰기 카운터를
   다시 기록한다.
2. 행 데이터를 로그나 중간 파일로 남기지 않고 SSH 표준 출력에서 PostgreSQL 입력으로
   직접 스트리밍한다.
3. 한 PostgreSQL transaction 안에서 명시적 컬럼 목록과 ID로 삽입한다. 충돌이나 변환
   오류가 하나라도 발생하면 transaction 전체를 rollback한다.
4. `security_audit_log_id_seq`는 `MAX(id)=4`, `notification_schedule_id_seq`는
   `MAX(id)=1`을 기준으로 조정해 다음 ID가 각각 `5`, `2`가 되게 한다.
5. commit 전후에 source/target exact count, NULL/boolean/JSON/FK 검증과 비식별 hash
   대조를 수행한다.
6. 적재 후에도 운영 애플리케이션은 MySQL `prod` 프로필을 유지한다. OpenBao 변경,
   Supabase 프로필 전환과 배포는 별도 cutover 승인 전에는 수행하지 않는다.

## 2026-08-01 데이터 적재 리허설 실행 결과

마스터 승인 후 원본 MySQL의 `security_audit_log` 4행과 `notification_schedule` 1행을
Supabase PostgreSQL에 적재했다.

- 적재 직전 source exact count와 `MAX(id)`는 각각 `4/4`, `1/1`로 기준선과 같았다.
- MySQL 행은 SSH 표준 출력에서 hex encoding한 뒤 로컬 importer의 표준 입력으로 직접
  전달했다. 행 내용이나 개인정보를 도구 출력에 표시하지 않았고 중간 파일도 만들지 않았다.
- Supabase 비밀번호는 1Password `op run --`으로 importer 환경에만 주입했다.
- importer는 Flyway version `2`, 대상 6개 테이블 0행, RLS 6개 활성화, 정책 0개를
  transaction 시작 시 다시 확인했다.
- 명시적 컬럼 목록과 원본 ID로 두 테이블을 한 transaction 안에서 삽입했다.
- canonical SHA-256은 transaction 안에서 source stream과 삽입 결과를 비교했으며
  실제 hash 값은 출력하지 않았다. 결과는 `hash_match=true`였다.
- FK orphan은 `0`건이었다.
- 모든 검증 통과 후 transaction을 commit했다.

독립적인 사후 읽기 검증 결과는 다음과 같다.

| check | result |
| --- | --- |
| Flyway V1/V2 | 모두 `success=true` |
| `security_audit_log` target rows | `4` |
| `notification_schedule` target rows | `1` |
| 나머지 target application tables | 모두 `0` |
| `security_audit_log_id_seq` | last `4`, next `5` |
| `notification_schedule_id_seq` | last `1`, next `2` |
| RLS enabled tables | `6` |
| Data API policies | `0` |
| FK orphan | `0` |

사후 원본 MySQL도 두 테이블의 exact count와 `MAX(id)`가 각각 `4/4`, `1/1`로 유지됐고
`Com_insert`, `Com_update`, `Com_delete`, `Com_replace` 변화가 없었다. 원본에는 어떤 쓰기나
설정 변경도 수행하지 않았다.

현재 Supabase에는 승인된 5행의 리허설 데이터가 있으며 운영 애플리케이션은 계속 MySQL
`prod` 프로필을 사용한다. 따라서 이후 MySQL에 신규 쓰기가 발생하면 cutover 전에 해당
증분을 다시 계산하고 동기화해야 한다. OpenBao secret 반영, 런타임 프로필 전환, 배포와
MySQL 중지는 수행하지 않았다.

## 2026-08-01 cutover 준비 상태 점검

데이터 적재 리허설 후 다음 gate를 값 변경 없이 읽기 전용으로 점검했다.

- 운영 MySQL의 데이터는 Docker volume `docker_mysql_data`에 있으며 컨테이너에서
  `/var/lib/mysql`로 사용한다.
- 저장소에는 이 MySQL volume을 대상으로 하는 `mysqldump`/`mysqlpump` 자동화가 없다.
  이후 승인된 수동 백업·격리 복구 시험을 수행했으며 결과는 아래 실행 기록에 남겼다.
- OpenBao sidecar가 렌더링한 파일 이름만 확인했다. 기존 `MYSQL_*` key는 존재하지만
  `SUPABASE_DB_URL`, `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`는 아직 없다.
- secret 값은 읽지 않았으며 OpenBao state도 변경하지 않았다.

따라서 운영 cutover 전에 다음 두 작업을 순서대로 완료해야 한다.

1. 운영 MySQL의 일관된 백업을 생성하고 격리된 임시 MySQL에서 복구 시험과 exact count를
   검증한다. 이 단계는 아래 기록과 같이 완료했다.
2. 승인된 보안 입력 경로로 OpenBao에 `SUPABASE_DB_*` 3개 key를 등록한 뒤 sidecar
   렌더링과 `supabase` 프로필 startup smoke test를 수행한다.

## 2026-08-02 MySQL 백업·격리 복구 시험

마스터 승인 후 운영 MySQL을 중지하지 않고 `mysqldump 8.0.35`의
`--single-transaction --quick` 방식으로 일관된 논리 백업을 생성했다.

- 백업 파일: `/Users/nanamix/.config/korean-fortune-system/backups/mysql/korean_fortune_20260802T014838+0900.sql.gz`
- 크기: `3234` bytes
- 권한: `-rw-------` (`nanamix:staff`), 상위 디렉터리 `drwx------`
- SHA-256: `238dc9729a78250fb71ea1b88b63612d26c05f31fb143ee49b2d079d763ec2f1`
- `gzip -t`: 성공
- 운영 MySQL 비밀번호는 컨테이너 내부 `MYSQL_ROOT_PASSWORD_FILE`에서만 소비했고
  명령행, 로그, 백업 경로에 노출하지 않았다.

복구 시험은 `mysql:8.0.35` 임시 컨테이너에서 수행했다. 컨테이너는 `--network none`으로
외부 네트워크를 차단하고 `/var/lib/mysql`을 `tmpfs`로 구성해 영구 volume을 사용하지
않았다. 복구 완료 후 다음 snapshot vector가 백업 시점 기대값과 정확히 일치했다.

`flyway_schema_history|fortune_cache|notification_schedule|max(notification_schedule.id)|saju_data|security_audit_log|max(security_audit_log.id)|sinsal_master|tojeong_gwa|user_roles|users`

`2|0|1|1|0|4|4|0|0|0|0`

9개 테이블의 `CHECK TABLE` 결과는 모두 `OK`였다. 검증용 컨테이너와 임시 secret file은
삭제해 잔존 개수가 각각 `0`임을 확인했다. 백업 파일만 rollback 자산으로 보존했다.
시험 후 운영 `korean-fortune-mysql`, `docker-app-1`, `korean-fortune-openbao-secrets`는
모두 Docker health `healthy`를 유지했다. 운영 MySQL volume, 애플리케이션 프로필,
OpenBao state에는 변경하지 않았다.

## 2026-08-02 OpenBao 등록·격리 startup smoke

마스터 승인 후 OpenBao의 `secret/projects/korean-fortune-system/prod`에 다음 세 key를
CAS가 적용된 한 번의 원자적 patch로 등록했다.

- `SUPABASE_DB_URL`
- `SUPABASE_DB_USER`
- `SUPABASE_DB_PASSWORD`

변경 전 rollback 기준은 KV version `13`, 변경 후 현재 version은 `14`다. URL과 user는
확정된 Supavisor Session Pooler 값과 정확히 일치하고 password는 non-empty 문자열임을
값 비노출 검증으로 확인했다. password는 1Password `op://` 참조를 `op run`으로 해석한
뒤 stdin으로만 OpenBao CLI에 전달했으며 명령 인자, 로그, 임시 파일에 기록하지 않았다.

운영 sidecar와 secret volume을 건드리지 않고 별도 1 MiB tmpfs volume과 임시 renderer를
사용했다. 기존 운영 필수 key 6개와 Supabase key 3개를 모두 required key로 지정했으며,
renderer `.ready` 생성과 Supabase 세 파일의 non-empty·mode `0444`를 확인했다.

현재 운영 앱과 동일한 image를 포트 공개 없이 임시 실행한 startup smoke 결과는 다음과
같다.

- `supabase` 단독 프로필: PostgreSQL `17.6` Hikari 연결과 Flyway 인식까지 성공했으나
  `prod`의 mail 설정이 적용되지 않아 `JavaMailSender` bean 부재로 context가 종료됐다.
- `prod,supabase` 조합: `prod`의 운영 설정을 유지하면서 뒤의 `supabase`가 datasource,
  JPA, Flyway를 덮어썼다. 애플리케이션이 정상 시작했고 Actuator health는 `UP`이었다.
- smoke 중 외부 AI, startup canary, receipt cleanup과 예약 알림 cron은 비활성화했다.
- 임시 앱, renderer, tmpfs volume은 모두 삭제했다.

따라서 DB-only 운영 cutover의 profile 값은 `supabase` 단독이 아니라
`prod,supabase`여야 한다. 검증 후 운영 `docker-app-1`은 계속 `prod`를 사용하고 MySQL에
연결돼 있으며 운영 컨테이너 3개는 모두 Docker health `healthy`다. OpenBao 등록까지만
완료했고 운영 sidecar 재렌더링, 쓰기 동결, 최종 증분 이관과 앱 교체는 수행하지 않았다.

## 2026-08-02 운영 cutover 실행 기록

마스터 승인 후 다음 순서로 실제 DB-only cutover를 수행했다.

1. 운영 앱을 정상 종료해 API 쓰기와 `@Scheduled` 실행을 함께 동결했다.
2. 종료 전후 MySQL snapshot vector를 대조했다. 두 시점 모두
   `security_audit_log=4/max4`, `notification_schedule=1/max1`, 나머지 업무 테이블은
   `0`으로 같았다.
3. Supabase를 read-only transaction으로 다시 조회했다. Flyway 성공 이력 `2`,
   `security_audit_log=4/max4`, `notification_schedule=1/max1`, 나머지 업무 테이블 `0`,
   RLS `6`, policy `0`으로 source와 일치했다. 증분 데이터가 없어 추가 적재는 생략했다.
4. 운영 sidecar를 Supabase key까지 포함한 필수 key 9개 계약으로 재렌더링했다.
5. 기존 운영과 동일한 immutable image
   `ghcr.io/nanamix/korean-fortune-system@sha256:7090d09a70ba8721a98f457a3b6c39f81ddd406aa1203192aa8dbd484f785022`를
   `prod,supabase` 프로필로 재생성했다.

첫 앱 교체 명령은 임시 Compose YAML의 cron `-` 인용 오류로 파싱 단계에서 실패했다.
rollback trap이 같은 immutable image의 기존 `prod` 앱을 즉시 재생성했고 Docker health
`healthy`를 확인했다. 이 짧은 rollback 구간 후 앱을 다시 정지하고 MySQL snapshot을
재대조했으며 변화가 없었다. JSON override로 인용 문제를 제거한 두 번째 교체는 성공했다.

운영 앱 검증 결과는 다음과 같다.

- active profiles: `prod,supabase`
- Docker health: `healthy`
- 내부 aggregate Actuator health: `UP`
- startup log: `Started KoreanFortuneApplication`
- public Cloudflare Access 경계: 미인증 요청 `HTTP 302`
- 내부 정적 UI와 세부 health endpoint: Cloudflare JWT가 없으면 `HTTP 401`로 차단
- source MySQL과 target Supabase snapshot: cutover 후에도 동일

기존 활성 예약 1건은 `08:40 Asia/Seoul`, 마지막 실행일 `2026-08-01`, 채널
`discord/default`였다. cutover 시점에는 이미 당일 예약 시간이 지나 scheduler를 재개하면
즉시 외부 발송될 수 있으므로 테스트 수신처 승인 없이 실행하지 않았다.
수동 cutover 앱에는 `APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON=-` 환경변수를 직접 적용해
scheduler를 비활성화했다. OpenBao version `16`에도 같은 대문자 key를 저장했지만,
이 값은 아래 자동 배포 후 `@Scheduled`의 소문자 dotted placeholder에 적용되지 않는 것으로
확인됐다. version `15`에는 CLI stdin 표기 해석으로 빈 값이 한 번 기록됐고 version `16`에서
literal `-`로 교정했던 이력이 있다.

현재 완료 범위는 DB cutover와 비인증 health 검증까지다. 사용자 Cloudflare Access 로그인
후 운세 조회·예약 CRUD 브라우저 E2E와 테스트 수신처 예약 발송, scheduler 재개는 별도
승인이 필요하다. MySQL 컨테이너·volume과 OpenBao의 MySQL key는 rollback 검증이 끝날
때까지 삭제하지 않는다.

### GitHub publish·자동 배포 영수증

- cutover commit: `d1cf45826ebf975b942f31af9ee69554711b6a39`
- CI/CD run: `30727802919`
- Build & Test: 성공
- Docker Build & Push: 성공
- Deploy to Production: 성공
- Discord Deployment Status: 성공
- 배포 image: `ghcr.io/nanamix/korean-fortune-system@sha256:6031a9e8349b336389c45da1e2419bbcaa9064766195fd9e188b12b7f64d8284`

자동 전체 Compose 교체 후에도 앱 profile은 `prod,supabase`, 앱·MySQL·OpenBao sidecar는
모두 Docker health `healthy`, 내부 aggregate Actuator는 `UP`이었다. OpenBao 필수 key
9개와 scheduler 비활성 파일을 재확인했다. source MySQL snapshot은
`4|4|1|1|0|0|0|0`, target Supabase 검증 vector는
`2|4|4|1|1|0|0|0|0|6|0`으로 cutover 기준과 같았다. 외부 Cloudflare Access 경계도
미인증 요청에 `HTTP 302`를 유지했다.

이 영수증만 추가하는 후속 문서 커밋은 재배포가 불필요하므로 `[skip ci]`로 push한다.

### 인증 E2E와 scheduler 교정

Cloudflare Access 로그인 후 운영 브라우저에서 다음을 검증했다. 화면의 개인정보와 운세
본문은 운영 기록에 남기지 않았다.

- 사주팔자 계산 API 호출과 결과 표·해석 렌더링: 성공
- 오늘운세 API 호출과 핵심·분야별 결과 렌더링: 성공
- 시스템 화면: `RUNNING`, 필수 구성 요소 정상, database `PostgreSQL` 표시
- 이관된 예약 1건 목록 조회: 성공
- 예약 `enabled`를 `true → false → true`로 변경하는 PATCH: 성공, 원래 상태로 복원

자동 배포된 앱에는 cron 환경변수가 없었다. configtree에 렌더링된 대문자
`APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON` 파일은
`${app.fortune.notification-schedule.cron:...}` placeholder를 비활성화하지 못했고,
`2026-08-02 10:55 Asia/Seoul`에 기존 `discord/default` 예약 1건이 실제 발송됐다.
로그의 `scheduleId=1`, `date=2026-08-02`, `channel=discord` 완료 기록과 DB의
`last_run_date=2026-08-02`로 확인했다. 일일 중복 방지 조건 때문에 같은 날 추가 발송은
발생하지 않는다.

즉시 동일 image를 `APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON=-` 환경으로 재생성해 Docker
health `healthy`를 확인했다. 재발 방지를 위해 운영 Compose에 이 환경변수의 기본값 `-`를
추가했다. 효과가 없던 OpenBao scheduler key는 CAS version `17`에서 제거했고 Supabase
key가 보존됐음을 확인했다. 테스트 전용 Discord target이 등록되고 별도 발송 승인을 받을
때까지 scheduler는 환경변수로 비활성 상태를 유지한다.

영구 수정 커밋 `32d18d3`의 CI/CD run `30728709391`은 Build & Test, Docker Build &
Push, Production Deploy를 모두 통과했다. 배포된 immutable image는
`ghcr.io/nanamix/korean-fortune-system@sha256:4815a5210dc8f68d63e677dea27050ed204846bc08fcbb4bb077f91a169adfc5`다.
운영 호스트에서 다음을 다시 확인했다.

- app active profiles: `prod,supabase`
- app cron 환경변수: `APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON=-`
- app·MySQL·OpenBao sidecar: Docker health `healthy`
- 내부 aggregate Actuator: `UP`
- 새 배포 후 예약 발송 완료 로그: `0`건
- OpenBao KV current version: `17`; Supabase key 3개 present, scheduler key absent
- MySQL/Supabase 공통 snapshot: `security_audit_log=4/max4`,
  `notification_schedule=1/max1`, 나머지 이관 대상 `0`
- Supabase: Flyway 성공 `2`, RLS `6`, policy `0`; 예약은
  `enabled=true`, `WAITING`, `last_run_date=2026-08-02`

### 테스트 Discord target 등록

마스터가 회전한 테스트 webhook을 1Password `supabase korean-fortune-system` 항목의
`DISCORD_WEBHOOK_URL_TEST` concealed 필드에 저장한 뒤, 값은 출력하지 않고 `op://`
참조와 `op run --`으로만 OpenBao에 전달했다. renderer AppRole을 이용한 첫 CAS patch는
권한 부족으로 `HTTP 403`을 반환했고 KV version `17`과 기존 값은 바뀌지 않았다.

운영 OpenBao 저장소의 승인된 stdin `bao kv patch` 경로로 `-cas=17` 단일 patch를 다시
수행해 KV version `18`에 `DISCORD_WEBHOOK_URL_TEST`를 추가했다. Supabase key 3개가
보존되고 scheduler configtree key가 계속 absent임을 값 비노출 조건으로 검증했다.
OpenBao renderer와 앱만 같은 immutable image로 재생성한 결과는 다음과 같다.

- renderer와 앱 secret volume: `DISCORD_WEBHOOK_URL_TEST` non-empty file present
- 앱 startup: `defaultConfigured=true`, `namedTargets=[test]`
- app active profiles: `prod,supabase`
- app cron 환경변수: `APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON=-`
- app Docker health: `healthy`; 내부 aggregate Actuator: `UP`
- 재생성 후 예약 발송 완료 로그: `0`건

target 등록 단계에서는 Discord 메시지를 보내지 않았다. 이후 마스터의 1회 테스트 발송
승인 시점에 운영 UI에서 마스터가 직접 전송 요청을 실행했으며, 앱 로그에서 다음 두 건을
확인했다.

- `2026-08-02 11:50:17 Asia/Seoul`: Discord 테스트 요청 및 1 chunk 전송 완료
- `2026-08-02 11:50:24 Asia/Seoul`: Discord 테스트 요청 및 1 chunk 전송 완료

두 요청 모두 직접 입력 URL 없이 처리됐고, 당시 UI에서 확인된 선택값은 서버 기본 OpenBao
Webhook이었다. 두 발송은 마스터가 직접 실행한 요청이므로 자동화 오작동으로 분류하지
않는다. 다만 `test` alias 선택 증거는 확보하지 못했으므로 해당 alias 전달 성공으로는
인정하지 않는다. 사후 확인에서 앱은 `healthy`, Actuator `UP`, profile `prod,supabase`,
cron `-`를 유지했고 예약 발송 완료 로그는 `0`건이었다. `test` alias의 실제 전달 검증과
scheduler 재개는 다시 별도 승인 후 수행한다.

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
4. OpenBao에 Supabase key를 등록하고 런타임 프로필을 `prod,supabase`로 전환한다.
5. Flyway version, Hibernate validate, Actuator health, 로그인, 운세 조회, 예약 CRUD를 확인한다.
6. 테스트 수신처로만 예약 발송을 확인한다.

## rollback

- cutover 전 MySQL을 삭제하거나 volume을 정리하지 않는다.
- rollback 시 애플리케이션 쓰기를 다시 차단한 뒤 MySQL 프로필과 기존 secret을 복원한다.
- Supabase에서 발생한 신규 쓰기가 있으면 데이터 손실 범위를 먼저 계산한다. 양쪽 DB를
  동시에 쓰기 가능 상태로 두지 않는다.
- rollback 검증이 끝날 때까지 MySQL은 read-only 보존 대상으로 취급한다.

# 12. CI/CD 및 운영

> GitHub Actions CI(`ci.yml`)·CD(`cd.yml`) 파이프라인 단계, 배포 흐름, 운영 점검(actuator/health, 로그)을 정리합니다.
> 관련 문서: [11. Docker 배포](./11-deployment-docker.md) · [07. 보안 및 관측성](./07-security-and-observability.md) · [README 인덱스](./README.md)

---

## 12.1 CI 파이프라인 (`.github/workflows/ci.yml`)

워크플로우 이름: `CI/CD Pipeline`. Java 21(Corretto), 앱 버전 env `2.6.0`, 레지스트리 `ghcr.io`.

트리거:

- `push` → `master`, `develop` 브랜치
- `pull_request` → `master` 브랜치

잡 구성 (순차 의존):

| 잡 | 실행 조건 | 단계 요약 |
|----|-----------|-----------|
| **build-and-test** | 항상 | ① checkout ② JDK 21(corretto, gradle 캐시) ③ `chmod +x gradlew` ④ `./gradlew bootJar --no-daemon -x test` ⑤ `./gradlew test --no-daemon` ⑥ 테스트 결과 업로드(`build/reports/tests/`, 7일) ⑦ JAR 업로드(`korean-fortune-app.jar`, 1일) |
| **docker** | `master` push 시에만 | Buildx 셋업 → GHCR 로그인 → 메타데이터 태그 추출 → `Dockerfile` `runtime` 타깃 빌드·푸시 (GHA 캐시), 태그 `latest` / `sha-<short>` / semver |
| **notify** | `master` push 시 | docker 잡 성공/실패 알림 출력 |

이미지 태그 정책(docker 잡): `latest`(기본 브랜치), `sha-<short>`, `semver`.

---

## 12.2 CD 파이프라인 (`.github/workflows/cd.yml`)

워크플로우 이름: `CD`. 레지스트리 `ghcr.io`.

트리거:

- `push` → `master`
- `workflow_dispatch` (입력: `environment` = `prod` | `dev`)

잡 구성:

### build-and-push

① checkout ② Amazon Corretto 21 셋업 ③ `chmod +x gradlew` ④ `./gradlew bootJar --no-daemon -x test` ⑤ GHCR 로그인 ⑥ 메타데이터 태그 추출(`branch` / `sha-` / `latest`) ⑦ Buildx ⑧ `Dockerfile` `runtime` 타깃 빌드·푸시(GHA 캐시, build-args: `BUILD_DATE`, `VCS_REF`, `VERSION`) ⑨ 이미지 digest 출력.

출력: `image-tag`, `image-digest`.

### deploy (`master` 한정, environment: production)

`vars.DEPLOY_HOST` 가 설정된 경우 `appleboy/ssh-action` 으로 원격 서버에 SSH 배포:

1. GHCR 로그인(`secrets.GHCR_PAT`)
2. `docker pull ...:latest`
3. 배포 디렉토리(`vars.DEPLOY_PATH`, 기본 `/opt/korean-fortune`)에서 기존 컨테이너 down
4. `docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d`
5. 헬스체크 폴링: `http://localhost:8080/actuator/health` 를 최대 20회(10초 간격) 재시도, 200이면 성공, 실패 시 앱 로그 출력 후 종료
6. `docker compose ps` 로 상태 확인

`DEPLOY_HOST` 미설정 시 배포는 스킵되고, 이미지는 GHCR에 푸시된 상태로 남습니다.

배포에 필요한 GitHub 설정:

| 종류 | 이름 | 용도 |
|------|------|------|
| variable | `DEPLOY_HOST` / `DEPLOY_USER` / `DEPLOY_PORT` / `DEPLOY_PATH` / `APP_URL` | SSH 대상·경로·앱 URL |
| secret | `DEPLOY_SSH_KEY` | SSH 개인키 |
| secret | `GHCR_PAT` | 서버의 `docker login` 용 PAT (`read:packages`) |

---

## 12.3 배포 흐름 요약

```
개발자 push (master)
   │
   ├─ ci.yml: build-and-test → docker(빌드·푸시 latest/sha) → notify
   │
   └─ cd.yml: build-and-push(GHCR) → deploy(SSH pull & compose up → 헬스체크)
```

수동 배포(서버에서 직접):

```bash
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml pull
docker compose -f docker/docker-compose.yaml -f docker/docker-compose.prod.yaml up -d
```

---

## 12.4 운영 점검

### 헬스체크 / 상태

```bash
curl -f http://localhost:8080/actuator/health      # 시스템 상태
curl http://localhost:8080/actuator/info           # 빌드 정보
curl http://localhost:8080/api/system/status       # 애플리케이션 상태 API
```

노출된 Actuator 엔드포인트: `health`, `info`, `metrics`, `prometheus`, `caches` (운영 프로필 기준). 상세는 [07. 보안 및 관측성](./07-security-and-observability.md) 참고.

### 로그 위치

| 환경 | 경로 |
|------|------|
| 로컬 실행 | `logs/korean-fortune.log` |
| 운영(prod) | `/app/logs/korean-fortune.log` (max 100MB, 30일 보관) |
| Docker | 컨테이너 `/app/logs` (base 스택에서 `logs` 볼륨으로 마운트) |

로그 라인에는 `trace=<traceId> span=<spanId>` 가 포함되어 분산 추적과 연계됩니다.

```bash
# 로컬 로그 팔로우
tail -f logs/korean-fortune.log

# 컨테이너 로그
docker compose -f docker/docker-compose.yaml logs -f app
```

### 메트릭 / 대시보드

- Prometheus: `http://localhost:8080/actuator/prometheus` (Compose Prometheus가 5초 간격 스크레이프, job `korean-fortune-app`)
- Grafana: `http://localhost:3000` (기본 비밀번호 `GRAFANA_PASSWORD`, 기본 `admin123`)

### 배포 롤백 참고

문제 발생 시 이전 이미지 태그(`sha-<short>`)로 되돌려 배포합니다. GHCR의 태그 목록에서 직전 정상 커밋의 `sha-` 태그를 선택해 `docker compose` 이미지 참조를 교체하거나 서버에서 해당 태그를 pull 후 재기동합니다.

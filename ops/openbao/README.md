# OpenBao runtime secret rendering

운영 secret은 OpenBao KV v2의 `secret/projects/korean-fortune-system/<environment>`에 저장한다. Compose 기동 시 `openbao-secrets`가 Tailscale 사설 HTTPS 경로로 OpenBao에 접속하고 AppRole로 인증한 뒤 값을 공유 `tmpfs`에 파일로 렌더링한다. Git 작업 트리, Docker 이미지, `.env`, Docker inspect에는 값이 남지 않는다.

기본 endpoint는 `https://jyha-macbook-pro-2019.tail21796d.ts.net:8443`이다. 2019 Mac의 Tailscale Serve가 HTTPS 8443을 `http://127.0.0.1:8200`으로 proxy하며, tailnet ACL을 통과한 장치에서만 접근할 수 있다. OpenBao 자체 Docker port는 계속 loopback에만 bind한다.

## 필수 KV key

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `GRAFANA_PASSWORD`
- `JWT_SECRET` (UTF-8 기준 32 byte 이상)

다음 선택 key도 같은 경로에 문자열로 저장할 수 있다.

- `OPENAI_API_KEY`
- `DEEPSEEK_API_KEY` (기본 외부 AI 제공자)
- `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`
- `DISCORD_WEBHOOK_URL`
- `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`, `FORTUNE_EMAIL_ENABLED`
- `REDIS_PASSWORD`
- `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `APP_FORTUNE_AI_ENABLED`, `APP_FORTUNE_AI_PROVIDER`, `APP_FORTUNE_AI_MODEL`, `APP_FORTUNE_AI_BASE_URL`

KV key는 `^[A-Z][A-Z0-9_]*$`, 값은 문자열이어야 한다. 값은 명령행 인자나 로그로 전달하지 말고 OpenBao UI 또는 승인된 보안 입력 경로로 등록한다.

## Bootstrap credential

기본 host directory는 `/Users/nanamix/.config/korean-fortune-system/openbao-bootstrap`다. 기본 Tailscale 모드에서는 아래 두 파일만 owner-only permission으로 관리한다.

- `approle-role-id`
- `approle-secret-id`

Bootstrap credential은 Git과 Compose YAML에 넣지 않는다. 필요하면 `OPENBAO_BOOTSTRAP_HOST_DIR`로 host directory만 변경한다.

운영 AppRole token은 15분 TTL, Secret ID는 30일 TTL과 최대 100회 사용으로 제한한다. Secret ID 만료 전에 새 값을 발급해 bootstrap 파일을 원자 교체하고, 새 endpoint smoke test가 끝날 때까지 직전 파일을 owner-only backup으로 유지한다.

Cloudflare Access 경로는 긴급 원복 전용으로만 유지한다. 원복할 때는 기존 두 Cloudflare bootstrap 파일을 보존한 상태에서 아래 환경 변수를 함께 지정한다.

```bash
OPENBAO_ADDR=https://vault.jyha.net
OPENBAO_EDGE_AUTH_MODE=cloudflare
```

이 모드에서만 `cf-access-client-id`, `cf-access-client-secret`을 읽고 Cloudflare Access header를 추가한다. 기본값인 `tailscale` 모드에서는 두 파일을 읽지 않는다.

## 실행

```bash
docker compose \
  -f docker/docker-compose.yaml \
  -f docker/docker-compose.prod.yaml \
  -f docker/docker-compose.openbao.override.yml \
  up -d
```

`openbao-secrets`는 렌더링 후 `.ready` health marker를 만들고 상주한다. Docker의 `local + type=tmpfs` volume은 mount한 writer가 종료되면 내용이 사라지므로, MySQL·애플리케이션·Grafana는 sidecar의 `service_healthy`를 기다린다. renderer는 필수 key가 없거나 인증·응답 검증에 실패하면 fail-closed로 종료하며, 성공한 sidecar가 상주하는 동안에만 secret 파일이 공유된다.

MySQL은 공식 이미지의 `MYSQL_*_FILE`, Grafana는 `GF_SECURITY_ADMIN_PASSWORD__FILE`, Spring Boot는 `configtree:`로 값을 읽는다. secret volume은 Docker local driver의 `tmpfs`이므로 컨테이너가 값을 파일로 소비하면서도 host disk에는 저장하지 않는다. directory는 비특권 애플리케이션이 key 목록을 읽을 수 있도록 `0755`, secret 파일은 값 쓰기를 막도록 `0444`로 유지한다. sidecar를 중지하면 tmpfs 내용도 사라지므로 소비 서비스 재기동 전에 sidecar health를 먼저 복구해야 한다.

## OpenBao project 준비

OpenBao 운영 저장소에서 프로젝트명 `korean-fortune-system`으로 policy와 AppRole을 준비한다. 이 작업은 OpenBao state와 credential을 변경하므로 실행 전 운영 승인을 받는다.

```bash
cd /Users/jyha/Dev/nanamix-vault
./scripts/provision-project.sh korean-fortune-system
```

기존 저장소·Git history에 노출된 자격증명은 OpenBao로 옮기는 것만으로 안전해지지 않는다. 반드시 새 값으로 회전하고, 필요하면 별도 승인 후 history 정리를 수행한다.

## Tailscale Serve 운영

2019 Mac에서 현재 구성을 확인한다.

```bash
/Applications/Tailscale.app/Contents/MacOS/Tailscale serve status --json
```

재구성이 필요할 때만 다음을 실행한다.

```bash
/Applications/Tailscale.app/Contents/MacOS/Tailscale serve --bg --yes --https=8443 127.0.0.1:8200
```

8443 endpoint만 원복하려면 다음을 실행한다.

```bash
/Applications/Tailscale.app/Contents/MacOS/Tailscale serve --https=8443 off
```

2019 Mac에서는 OrbStack이 443을 사용하므로 Tailscale Serve를 443에 구성하지 않는다. 변경 후에는 tailnet peer에서 `/v1/sys/health`가 HTTP 200이고 `sealed=false`인지 확인한다.

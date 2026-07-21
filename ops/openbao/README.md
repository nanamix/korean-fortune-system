# OpenBao runtime secret rendering

운영 secret은 OpenBao KV v2의 `secret/projects/korean-fortune-system/<environment>`에 저장한다. Compose 기동 시 `openbao-secrets`가 Cloudflare Access와 OpenBao AppRole로 인증하고 값을 공유 `tmpfs`에 파일로 렌더링한다. Git 작업 트리, Docker 이미지, `.env`, Docker inspect에는 값이 남지 않는다.

## 필수 KV key

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_PASSWORD`
- `GRAFANA_PASSWORD`
- `JWT_SECRET` (UTF-8 기준 32 byte 이상)

다음 선택 key도 같은 경로에 문자열로 저장할 수 있다.

- `OPENAI_API_KEY`
- `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`
- `DISCORD_WEBHOOK_URL`
- `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`, `FORTUNE_EMAIL_ENABLED`
- `REDIS_PASSWORD`
- `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `APP_FORTUNE_AI_ENABLED`, `APP_FORTUNE_AI_PROVIDER`, `APP_FORTUNE_AI_MODEL`, `APP_FORTUNE_AI_BASE_URL`

KV key는 `^[A-Z][A-Z0-9_]*$`, 값은 문자열이어야 한다. 값은 명령행 인자나 로그로 전달하지 말고 OpenBao UI 또는 승인된 보안 입력 경로로 등록한다.

## Bootstrap credential

기본 host directory는 `/Users/nanamix/.config/korean-fortune-system/openbao-bootstrap`다. 아래 네 파일을 owner-only permission으로 관리한다.

- `cf-access-client-id`
- `cf-access-client-secret`
- `approle-role-id`
- `approle-secret-id`

Bootstrap credential은 Git과 Compose YAML에 넣지 않는다. 필요하면 `OPENBAO_BOOTSTRAP_HOST_DIR`로 host directory만 변경한다.

## 실행

```bash
docker compose \
  -f docker/docker-compose.yaml \
  -f docker/docker-compose.prod.yaml \
  -f docker/docker-compose.openbao.override.yml \
  up -d
```

`openbao-secrets`가 성공적으로 종료된 뒤 MySQL, 애플리케이션, Grafana가 시작된다. renderer는 필수 key가 없거나 인증·응답 검증에 실패하면 fail-closed로 종료한다.

MySQL은 공식 이미지의 `MYSQL_*_FILE`, Grafana는 `GF_SECURITY_ADMIN_PASSWORD__FILE`, Spring Boot는 `configtree:`로 값을 읽는다. secret volume은 Docker local driver의 `tmpfs`이므로 컨테이너가 값을 파일로 소비하면서도 host disk에는 저장하지 않는다.

## OpenBao project 준비

OpenBao 운영 저장소에서 프로젝트명 `korean-fortune-system`으로 policy와 AppRole을 준비한다. 이 작업은 OpenBao state와 credential을 변경하므로 실행 전 운영 승인을 받는다.

```bash
cd /Users/jyha/Dev/nanamix-vault
./scripts/provision-project.sh korean-fortune-system
```

기존 저장소·Git history에 노출된 자격증명은 OpenBao로 옮기는 것만으로 안전해지지 않는다. 반드시 새 값으로 회전하고, 필요하면 별도 승인 후 history 정리를 수행한다.

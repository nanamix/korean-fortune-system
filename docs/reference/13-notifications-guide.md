# 13. 알림 연동 가이드 (Email · Telegram · Discord)

> 운세 결과를 이메일·텔레그램·Discord로 발송하기 위한 설정 가이드. Gmail·AWS SES 등 메일 공급자별 설정 포함.
> 관련: [설치 가이드](08-installation-guide.md) · [API 레퍼런스](05-api-reference.md)

## 요약

`*/calculate-and-send` 엔드포인트는 `notification` 객체로 발송 채널을 지정한다. 모든 채널은 **선택적**이며, 설정이 없으면 계산은 정상 수행되고 발송만 건너뛴다.

| 채널 | 활성화 조건 | 핵심 설정 |
|------|------------|-----------|
| 📧 이메일 | `app.fortune.email.enabled=true` + SMTP 자격증명 | `spring.mail.*` |
| 📱 텔레그램 | 봇 토큰 + 채팅 ID | `app.fortune.telegram.*` |
| 📢 Discord | 채널 Incoming Webhook URL | `app.fortune.discord.webhook-url` |

`notification.notificationType` 값: `email` · `telegram` · `discord` · `both`(email+telegram) · `all`(전체).

---

## 1. 이메일 (Email)

메일 발송은 Spring `JavaMailSender`(`spring-boot-starter-mail`)를 사용한다. 어떤 SMTP 공급자든 `spring.mail.*` 만 맞추면 된다.

### 공통 설정 키

```yaml
spring:
  mail:
    host: <SMTP 호스트>
    port: <포트>
    username: <SMTP 사용자>
    password: <SMTP 비밀번호/앱 비밀번호>
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
app:
  fortune:
    email:
      enabled: true   # 이 토글이 false 면 발송 스킵
```

- 발신자(from)는 `spring.mail.username` 을 사용한다(`EmailService`).
- **환경변수 주의**: `dev` 프로파일은 `${EMAIL_USERNAME}`/`${EMAIL_PASSWORD}` 를 참조한다. Docker/운영 스택의 `.env` 는 `MAIL_USERNAME`/`MAIL_PASSWORD` 를 쓰므로, 사용하는 프로파일의 실제 키 이름을 확인할 것. 헷갈리면 `spring.mail.username/password` 를 직접 지정하는 것이 가장 확실하다.

### 1-1. Gmail

Gmail 은 일반 비밀번호로 SMTP 로그인이 **불가**하다. **앱 비밀번호(App Password)** 를 발급해야 한다.

1. Google 계정 → 보안 → **2단계 인증**을 먼저 활성화.
2. 보안 → **앱 비밀번호**(App passwords) → 앱 이름 입력 → 16자리 비밀번호 생성.
3. 그 16자리를 `spring.mail.password` 에 넣는다(공백 제거).

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587           # STARTTLS
    username: you@gmail.com
    password: <16자리 앱 비밀번호>
```

- 포트 465(SSL) 사용 시: `properties.mail.smtp.ssl.enable=true`, starttls 비활성.
- 발송량이 많으면 Gmail 일일 한도(무료 ~500통/일)에 걸린다 → 운영은 SES 권장.

### 1-2. AWS SES (Simple Email Service)

운영 환경 권장. 대량·고신뢰 발송에 적합하다.

1. **SES 콘솔** → 보내는 도메인 또는 이메일 **검증(Verify)**.
2. **샌드박스 해제**: 신규 계정은 샌드박스 상태라 *검증된 수신자에게만* 발송된다. 프로덕션 접근을 신청해 해제.
3. **SMTP 자격증명 생성**: SES → SMTP settings → *Create SMTP credentials* → IAM 사용자와 SMTP username/password 발급(콘솔에서 1회만 표시).
4. 리전별 SMTP 엔드포인트 사용:

| 리전 | SMTP 엔드포인트 |
|------|-----------------|
| ap-northeast-2 (서울) | `email-smtp.ap-northeast-2.amazonaws.com` |
| us-east-1 (버지니아) | `email-smtp.us-east-1.amazonaws.com` |
| eu-west-1 (아일랜드) | `email-smtp.eu-west-1.amazonaws.com` |

```yaml
spring:
  mail:
    host: email-smtp.ap-northeast-2.amazonaws.com
    port: 587           # 또는 2587 (ISP가 587 차단 시)
    username: <SES SMTP username>   # AKIA... 형태가 아니라 SES 전용 SMTP 사용자
    password: <SES SMTP password>
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

- **from 주소는 반드시 SES 에서 검증된 주소**여야 한다(`spring.mail.username` 이 보낸사람이므로, SES 검증 주소로 맞추거나 `EmailService` 발신자 설정을 검증 주소로).
- IAM 정책은 최소권한: `ses:SendRawEmail` 만 부여.
- 자격증명은 코드·커밋 금지 → 환경변수/Secrets Manager.

### 1-3. 기타 SMTP (사내 메일, Naver, Outlook 등)

호스트/포트/인증 방식만 공급자 문서대로 맞추면 동일하게 동작한다. 예) Naver `smtp.naver.com:587`, Outlook `smtp.office365.com:587`.

---

## 2. 텔레그램 (Telegram)

1. [@BotFather](https://t.me/BotFather) → `/newbot` → 봇 토큰 발급.
2. 봇과 대화를 시작하고, 채팅 ID 확인(`https://api.telegram.org/bot<TOKEN>/getUpdates` 의 `chat.id`).

```yaml
app:
  fortune:
    telegram:
      bot-token: ${TELEGRAM_BOT_TOKEN:}
      chat-id: ${TELEGRAM_CHAT_ID:}
```

요청별 채팅 ID 는 `notification.telegramChatId`(숫자) 로 덮어쓸 수 있다.

---

## 3. Discord

Discord 는 채널 **Incoming Webhook** 으로 발송한다(봇 등록 불필요).

1. 대상 채널 → **채널 편집** → **연동(Integrations)** → **웹후크(Webhooks)** → **새 웹후크** → URL 복사.
2. URL 형식: `https://discord.com/api/webhooks/<id>/<token>`

```yaml
app:
  fortune:
    discord:
      webhook-url: ${DISCORD_WEBHOOK_URL:}
```

- 요청별 webhook 은 `notification.discordWebhookUrl` 로 지정 가능. **SSRF 방지**를 위해 Discord 공식 호스트(`discord.com`/`discordapp.com`/`canary`/`ptb`)의 `/api/webhooks/` URL 만 허용된다(그 외는 발송 차단).
- 메시지는 평문 `content`(최대 2000자, 초과 시 절단).

### 테스트

```bash
curl -X POST http://localhost:8080/api/fortune/discord/test \
  -H "Content-Type: application/json" \
  -d '{"message":"테스트","webhookUrl":"https://discord.com/api/webhooks/…"}'
```

---

## 4. 발송 요청 예시

```bash
curl -X POST http://localhost:8080/api/fortune/saju/calculate-and-send \
  -H "Content-Type: application/json" \
  -d '{
    "birthYear":1981,"birthMonth":3,"birthDay":20,
    "birthHour":1,"birthMinute":59,"gender":"M","calendarType":"SOLAR",
    "notification":{
      "recipientName":"홍길동",
      "notificationType":"all",
      "email":"user@example.com",
      "telegramChatId":"123456789",
      "discordWebhookUrl":"https://discord.com/api/webhooks/…"
    }
  }'
```

`notificationType` 에 따라 해당 채널만 발송된다. 미설정 채널은 조용히 건너뛴다.

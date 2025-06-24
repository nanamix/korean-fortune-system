```mermaid
sequenceDiagram
    participant User as 👤 사용자
    participant Nginx as 🌐 Nginx
    participant App as 🚀 Spring Boot App
    participant Redis as 🔴 Redis
    participant MySQL as 🐬 MySQL
    participant Ollama as 🤖 Ollama
    participant OpenAI as 🤖 OpenAI

    User->>Nginx: 운세/사주/토정/AI API 요청
    Nginx->>App: 요청 전달
    App->>Redis: 캐시 조회
    alt 캐시 히트
        Redis-->>App: 캐시 결과
        App-->>Nginx: 응답
    else 캐시 미스
        App->>MySQL: DB 조회/저장
        App->>Ollama: AI 해석(로컬)
        App->>OpenAI: AI 해석(외부)
        App->>Redis: 캐시 저장
        App-->>Nginx: 응답
    end
    Nginx-->>User: 결과 반환

    %% 10. 모니터링 (비동기)
    Note over App, Redis: 📊 메트릭 수집 (비동기)
    App--)Nginx: API 호출 메트릭
    Nginx--)Redis: 응답 시간 메트릭
    App--)MySQL: DB 쿼리 메트릭
    App--)Ollama: AI 호출 메트릭
    App--)OpenAI: AI 호출 메트릭

    %% 스타일 정의
    rect rgb(240, 248, 255)
        Note right of User: 📱 클라이언트 계층
    end
    
    rect rgb(248, 255, 240)
        Note right of Nginx: 🛡️ 보안 & 게이트웨이
    end
    
    rect rgb(255, 248, 240)
        Note right of App: ⚙️ 비즈니스 로직
    end
    
    rect rgb(248, 240, 255)
        Note right of MySQL: 🗄️ 데이터 계층
    end
    
    rect rgb(255, 240, 248)
        Note right of Ollama: 🤖 Ollama
    end
    
    rect rgb(255, 240, 248)
        Note right of OpenAI: 🌍 외부 서비스
    end
    ```
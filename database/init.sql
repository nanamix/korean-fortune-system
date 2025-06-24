# init.sql (데이터베이스 초기화)
-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS korean_fortune CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 및 권한 부여
CREATE USER IF NOT EXISTS 'fortune_user'@'%' IDENTIFIED BY 'fortune_password';
GRANT ALL PRIVILEGES ON korean_fortune.* TO 'fortune_user'@'%';
FLUSH PRIVILEGES;

-- 기본 테이블 생성
USE korean_fortune;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사주 데이터 테이블
CREATE TABLE IF NOT EXISTS saju_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    birth_year INT NOT NULL,
    birth_month INT NOT NULL,
    birth_day INT NOT NULL,
    birth_hour INT NOT NULL,
    birth_minute INT NOT NULL,
    gender CHAR(1) NOT NULL,
    calendar_type VARCHAR(10) NOT NULL,
    year_pillar VARCHAR(2),
    month_pillar VARCHAR(2),
    day_pillar VARCHAR(2),
    time_pillar VARCHAR(2),
    day_master VARCHAR(1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_birth_date (birth_year, birth_month, birth_day)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 운세 이력 테이블
CREATE TABLE IF NOT EXISTS fortune_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    fortune_type VARCHAR(20) NOT NULL,
    target_date DATE NOT NULL,
    result_data JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_date (user_id, target_date),
    INDEX idx_fortune_type (fortune_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 샘플 데이터 삽입
INSERT IGNORE INTO users (username, email) VALUES
('admin', 'admin@fortune.com'),
('testuser', 'test@fortune.com');

---

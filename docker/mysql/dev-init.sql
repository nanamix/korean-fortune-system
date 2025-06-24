-- 개발용 초기 데이터
CREATE DATABASE IF NOT EXISTS korean_fortune_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE korean_fortune_dev;

-- 테스트 사용자 생성
CREATE USER IF NOT EXISTS 'fortune_user'@'%' IDENTIFIED BY 'fortune_dev_2025!';
GRANT ALL PRIVILEGES ON korean_fortune_dev.* TO 'fortune_user'@'%';
FLUSH PRIVILEGES;

-- 기본 테이블 (필요시 추가)
-- CREATE TABLE IF NOT EXISTS fortune_logs (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     user_id VARCHAR(50),
--     request_type VARCHAR(50),
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

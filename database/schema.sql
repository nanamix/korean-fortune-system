-- schema.sql
CREATE DATABASE IF NOT EXISTS korean_fortune 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE korean_fortune;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    profile_image_url VARCHAR(500),
    
    -- 생년월일 정보
    birth_year INT,
    birth_month INT,
    birth_day INT,
    birth_hour INT,
    birth_minute INT,
    gender ENUM('M', 'F'),
    calendar_type ENUM('SOLAR', 'LUNAR'),
    birth_location VARCHAR(100),
    
    -- OAuth2 정보
    auth_provider ENUM('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER', 'FACEBOOK') NOT NULL DEFAULT 'LOCAL',
    provider_id VARCHAR(100),
    
    -- 계정 상태
    enabled BOOLEAN DEFAULT TRUE,
    account_locked BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verified_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    login_count INT DEFAULT 0,
    failed_login_attempts INT DEFAULT 0,
    password_changed_at TIMESTAMP NULL,
    
    -- 사용자 설정
    preferred_language ENUM('KOREAN', 'ENGLISH', 'JAPANESE', 'CHINESE') DEFAULT 'KOREAN',
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',
    ai_features_enabled BOOLEAN DEFAULT TRUE,
    notification_enabled BOOLEAN DEFAULT TRUE,
    marketing_emails_enabled BOOLEAN DEFAULT FALSE,
    
    -- 타임스탬프
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    -- 인덱스
    INDEX idx_users_email (email),
    INDEX idx_users_username (username),
    INDEX idx_users_provider (auth_provider, provider_id),
    INDEX idx_users_created_at (created_at),
    INDEX idx_users_birth_date (birth_year, birth_month, birth_day)
);

-- 사용자 역할 테이블 (ElementCollection)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role ENUM('USER', 'ADMIN', 'MODERATOR', 'PREMIUM') NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 사주 데이터 테이블
CREATE TABLE IF NOT EXISTS saju_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    year_pillar VARCHAR(2) NOT NULL,
    month_pillar VARCHAR(2) NOT NULL,
    day_pillar VARCHAR(2) NOT NULL,
    time_pillar VARCHAR(2),
    day_master VARCHAR(1) NOT NULL,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 보안 감사 로그 테이블
CREATE TABLE IF NOT EXISTS security_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    ip_address VARCHAR(45),
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details JSON,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_action (action)
);

-- 신살 마스터 데이터 테이블
CREATE TABLE IF NOT EXISTS sinsal_master (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    korean_name VARCHAR(20) NOT NULL,
    description TEXT,
    is_lucky BOOLEAN NOT NULL,
    calculation_rule TEXT,
    impact_score INT DEFAULT 0
);

-- 토정비결 64괘 데이터
CREATE TABLE IF NOT EXISTS tojeong_gwa (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    gwa_number INT NOT NULL UNIQUE,
    gwa_name VARCHAR(10) NOT NULL,
    gwa_symbol VARCHAR(5) NOT NULL,
    summary TEXT NOT NULL,
    detailed_fortune TEXT NOT NULL,
    overall_score INT DEFAULT 50
);

-- 운세 캐시 테이블
CREATE TABLE IF NOT EXISTS fortune_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cache_key VARCHAR(200) NOT NULL UNIQUE,
    cache_data JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_cache_key (cache_key),
    INDEX idx_expires_at (expires_at)
);

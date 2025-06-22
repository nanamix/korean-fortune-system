-- schema.sql
CREATE DATABASE IF NOT EXISTS korean_fortune 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE korean_fortune;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    birth_year INT NOT NULL,
    birth_month INT NOT NULL,
    birth_day INT NOT NULL,
    birth_hour INT,
    birth_minute INT,
    gender ENUM('M', 'F') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    id INT PRIMARY KEY AUTO_INCREMENT,
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

-- 테스트용 데이터베이스 스키마
CREATE TABLE IF NOT EXISTS tojeong_gwa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gwa_number INT NOT NULL UNIQUE,
    gwa_name VARCHAR(10) NOT NULL,
    gwa_symbol VARCHAR(5) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    detailed_fortune TEXT NOT NULL,
    overall_score INT NOT NULL DEFAULT 50
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_tojeong_gwa_number ON tojeong_gwa(gwa_number); 
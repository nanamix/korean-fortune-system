-- 운영 환경을 위한 인덱스 생성
-- 이 파일은 운영 환경에서 별도로 실행

-- 토정비결 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_tojeong_gwa_score ON tojeong_gwa(overall_score);
CREATE INDEX IF NOT EXISTS idx_tojeong_records_year ON tojeong_user_records(target_year);
CREATE INDEX IF NOT EXISTS idx_tojeong_records_birth ON tojeong_user_records(birth_year, birth_month, birth_day);

-- 월별 운세 인덱스
CREATE INDEX IF NOT EXISTS idx_tojeong_monthly_gwa ON tojeong_monthly_fortune(gwa_number);
CREATE INDEX IF NOT EXISTS idx_tojeong_monthly_month ON tojeong_monthly_fortune(month_number);

-- 계절별 운세 인덱스
CREATE INDEX IF NOT EXISTS idx_tojeong_seasonal_gwa ON tojeong_seasonal_fortune(gwa_number);
CREATE INDEX IF NOT EXISTS idx_tojeong_seasonal_season ON tojeong_seasonal_fortune(season); 
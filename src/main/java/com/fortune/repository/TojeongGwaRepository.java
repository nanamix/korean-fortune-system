package com.fortune.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fortune.entity.TojeongGwaEntity;

import java.util.List;
import java.util.Optional;

/**
 * 🔮 토정비결 괘 정보를 관리하는 리포지토리
 * 
 * <p>이 리포지토리는 토정비결 괘 정보를 데이터베이스에서 조회하고 관리하는 역할을 합니다.</p>
 * 
 * @author 하진영
 */
@Repository
public interface TojeongGwaRepository extends JpaRepository<TojeongGwaEntity, Long> {
    Optional<TojeongGwaEntity> findByGwaNumber(Integer gwaNumber);
    List<TojeongGwaEntity> findByOverallScoreGreaterThanEqual(Integer score);
    List<TojeongGwaEntity> findByOverallScoreBetween(Integer minScore, Integer maxScore);
}

package com.fortune.repository;

import com.fortune.entity.TojeongGwaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface TojeongGwaRepository extends JpaRepository<TojeongGwaEntity, Long> {
    Optional<TojeongGwaEntity> findByGwaNumber(Integer gwaNumber);
    List<TojeongGwaEntity> findByOverallScoreGreaterThanEqual(Integer score);
    List<TojeongGwaEntity> findByOverallScoreBetween(Integer minScore, Integer maxScore);
}

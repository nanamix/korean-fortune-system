package com.fortune.repository;

import com.fortune.entity.NotificationSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationScheduleRepository extends JpaRepository<NotificationSchedule, Long> {
    List<NotificationSchedule> findByOwnerKeyOrderByCreatedAtDesc(String ownerKey);

    Optional<NotificationSchedule> findByIdAndOwnerKey(Long id, String ownerKey);

    List<NotificationSchedule> findByEnabledTrue();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationSchedule schedule
            set schedule.lastRunDate = :runDate,
                schedule.lastRunAt = :runAt,
                schedule.lastStatus = 'RUNNING',
                schedule.lastError = null
            where schedule.id = :id
              and (schedule.lastRunDate is null or schedule.lastRunDate <> :runDate)
            """)
    int claimRun(
            @Param("id") Long id,
            @Param("runDate") LocalDate runDate,
            @Param("runAt") LocalDateTime runAt);
}

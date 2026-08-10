package ru.danil.springtech.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.model.RetryableTask;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface RetryableTaskRepository extends CrudRepository<RetryableTask, UUID> {

    @Query(value = """
            SELECT *
            FROM retryable_task
            WHERE type = :type
              AND retry_time <= :now
              AND status = :status
              AND (lease_expires_at IS NULL OR lease_expires_at < :now)
            ORDER BY retry_time ASC
            FOR UPDATE SKIP LOCKED
            """,
            nativeQuery = true)
    List<RetryableTask> findRetryableTasks(RetryableTaskType type, Instant now, RetryableTaskStatus status, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE RetryableTask t
            SET t.status = :status
            WHERE t.id IN :ids
              AND t.status = :expectedStatus
            """)
    int updateStatusByIds(List<UUID> ids, RetryableTaskStatus status,  RetryableTaskStatus expectedStatus);

    @Modifying
    @Query("""
            UPDATE RetryableTask t
            SET t.status = :status
            WHERE t.id = :id
              AND t.status = :expectedStatus
            """)
    int updateStatusById(UUID id, RetryableTaskStatus status, RetryableTaskStatus expectedStatus);

    @Modifying
    @Query("""
    UPDATE RetryableTask t
    SET t.attempts = t.attempts + 1,
        t.retryTime = CASE WHEN (t.attempts + 1) < :maxAttempts THEN :nextRetry ELSE t.retryTime END,
        t.status = CASE WHEN (t.attempts + 1) >= :maxAttempts THEN :failedStatus ELSE :pendingStatus END,
        t.leaseExpiresAt = NULL
    WHERE t.id = :id
      AND t.status = :expectedStatus
    """)
    int incrementAttemptsAndReschedule(UUID id, Instant nextRetry, RetryableTaskStatus failedStatus, int maxAttempts, RetryableTaskStatus pendingStatus, RetryableTaskStatus expectedStatus);
}
package ru.danil.springtech.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT r FROM RetryableTask r
           WHERE r.type = :type
             AND r.retryTime <= :retryTime
             AND r.status = :status
           ORDER BY r.retryTime ASC
           """)
    List<RetryableTask> findRetryableTasks(RetryableTaskType type,
                                           Instant retryTime,
                                           RetryableTaskStatus status,
                                           Pageable pageable);

    @Modifying
    @Query("UPDATE RetryableTask t SET t.status = :status WHERE t.id IN :ids")
    void updateStatusByIds(List<UUID> ids, RetryableTaskStatus status);

    @Modifying
    @Query("UPDATE RetryableTask t SET t.status = :status WHERE t.id = :id")
    void updateStatusById(UUID id, RetryableTaskStatus status);
}
package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.config.RetryableTaskProperties;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.repository.RetryableTaskRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetryableTaskStatusService {
    private final RetryableTaskRepository retryableTaskRepository;
    private final RetryableTaskProperties retryableTaskProperties;

    @Transactional
    public void updateStatusById(UUID id,UUID leaseToken, RetryableTaskStatus status, RetryableTaskStatus expectedRetryableTaskStatus) {
        retryableTaskRepository.updateStatusById(id, leaseToken, status, expectedRetryableTaskStatus);
    }

    @Transactional
    public void reschedule(UUID retryableTaskId, UUID leaseToken) {
        Instant nextRetry = Instant.now().plus(Duration.ofSeconds(retryableTaskProperties.getRetryDelaySeconds()));
        int updated = retryableTaskRepository.incrementAttemptsAndReschedule(
                retryableTaskId,
                leaseToken,
                nextRetry,
                RetryableTaskStatus.FAILED,
                retryableTaskProperties.getMaxAttempts(),
                RetryableTaskStatus.PENDING,
                RetryableTaskStatus.PENDING
        );
        if (updated == 0) {
            log.warn("Задача {} уже не в статусе PENDING или не найдена", retryableTaskId);
        } else {
            log.info("Задача {} перепланирована (attempts увеличено)", retryableTaskId);
        }
    }
}
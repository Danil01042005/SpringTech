package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.config.RetryableTaskProperties;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.mapper.RetryableTaskMapper;
import ru.danil.springtech.model.RetryableTask;
import ru.danil.springtech.repository.RetryableTaskRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetryableTaskService {
    private final RetryableTaskRepository retryableTaskRepository;
    private final RetryableTaskMapper retryableTaskMapper;
    private final RetryableTaskProperties properties;

    @Transactional
    public RetryableTaskDTO createRetryableTask(PolicyDTO policyDTO, RetryableTaskType type) {
        RetryableTask retryableTask = retryableTaskMapper.toRetryableTask(policyDTO, type);
        retryableTask.setStatus(RetryableTaskStatus.PENDING);
        Instant instant = Instant.now();
        retryableTask.setRetryTime(instant);
        return retryableTaskMapper.toRetryableTaskDTO(retryableTaskRepository.save(retryableTask));
    }

    @Transactional
    public List<RetryableTaskDTO> getRetryableTasks(RetryableTaskType type) {
        Instant currentTime = Instant.now();
        Pageable pageable = PageRequest.of(0, properties.getLimit());
        List<RetryableTask> retryableTasks = retryableTaskRepository.findRetryableTasks(type, currentTime, RetryableTaskStatus.PENDING , pageable);

        for (RetryableTask retryableTask : retryableTasks) {
            retryableTask.setLeaseExpiresAt(currentTime.plus(Duration.ofSeconds(properties.getProcessingLeaseSeconds())));
        }
        return retryableTasks.stream().map(retryableTaskMapper::toRetryableTaskDTO).toList();
    }

    @Transactional
    public void updateStatusByIds(List<UUID> ids, RetryableTaskStatus status, RetryableTaskStatus expectedStatus) {
        retryableTaskRepository.updateStatusByIds(ids, status, expectedStatus);
    }

    @Transactional
    public void updateStatusById(UUID id, RetryableTaskStatus status, RetryableTaskStatus expectedRetryableTaskStatus) {
        retryableTaskRepository.updateStatusById(id, status, expectedRetryableTaskStatus);
    }

    @Transactional
    public void reschedule(UUID retryableTaskId) {
        RetryableTask retryableTask = returnRetryableTaskOrThrow(retryableTaskRepository.findById(retryableTaskId), retryableTaskId);
        int nextAttempts = retryableTask.getAttempts() + 1;
        if(nextAttempts > properties.getMaxAttempts()) {
            retryableTask.setStatus(RetryableTaskStatus.FAILED);
        } else {
            applyRetry(retryableTask, nextAttempts);
        }
    }

    private RetryableTask returnRetryableTaskOrThrow(Optional<RetryableTask> retryableTask, UUID retryableTaskId) {
        return retryableTask.orElseThrow(() -> {
            log.error("Человек с таким айди не найден {}", retryableTaskId);
            return new ObjectNotFoundException("Человек с таким айди не найден " + retryableTaskId);
        });
    }

    private void applyRetry(RetryableTask retryableTask, Integer nextAttempts) {
        retryableTask.setAttempts(nextAttempts);
        retryableTask.setStatus(RetryableTaskStatus.PENDING);
        retryableTask.setRetryTime(Instant.now().plus(Duration.ofSeconds(properties.getRetryDelaySeconds())));
        retryableTask.setLeaseExpiresAt(null);
    }
}

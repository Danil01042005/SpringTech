package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.config.RetryableTaskProperties;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.mapper.RetryableTaskMapper;
import ru.danil.springtech.model.RetryableTask;
import ru.danil.springtech.repository.RetryableTaskRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetryableTaskService {
    private final RetryableTaskRepository retryableTaskRepository;
    private final RetryableTaskMapper retryableTaskMapper;
    private final RetryableTaskProperties retryableTaskProperties;
    private final KafkaProducerService kafkaProducerService;
    @Lazy
    private final RetryableTaskService self;

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
        Pageable pageable = PageRequest.of(0, retryableTaskProperties.getLimit());
        List<RetryableTask> retryableTasks = retryableTaskRepository.findRetryableTasks(type, currentTime, RetryableTaskStatus.PENDING , pageable);

        for (RetryableTask retryableTask : retryableTasks) {
            retryableTask.setLeaseExpiresAt(currentTime.plus(Duration.ofSeconds(retryableTaskProperties.getProcessingLeaseSeconds())));
        }
        return retryableTasks.stream().map(retryableTaskMapper::toRetryableTaskDTO).toList();
    }

    @Transactional
    public void updateStatusByIds(List<UUID> ids, RetryableTaskStatus status, RetryableTaskStatus expectedStatus) {
        int updated = retryableTaskRepository.updateStatusByIds(ids, status, expectedStatus);
        if (updated != ids.size()) {
            log.warn("Обновлено статусов {} из {} задач (ожидаемый статус: {})", updated, ids.size(), expectedStatus);
        } else {
            log.debug("Успешно обновлены статусы {} задач", ids.size());
        }
    }

    @Transactional
    public void updateStatusById(UUID id, RetryableTaskStatus status, RetryableTaskStatus expectedRetryableTaskStatus) {
        retryableTaskRepository.updateStatusById(id, status, expectedRetryableTaskStatus);
    }

    @Transactional
    public void reschedule(UUID retryableTaskId) {
        Instant nextRetry = Instant.now().plus(Duration.ofSeconds(retryableTaskProperties.getRetryDelaySeconds()));
        int updated = retryableTaskRepository.incrementAttemptsAndReschedule(
                retryableTaskId,
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

    private List<UUID> awaitAndCollectSuccessfulIds(Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap) {
        List<UUID> successIds = new ArrayList<>();
        for (Map.Entry<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> entry : futureMap.entrySet()) {
            RetryableTaskDTO task = entry.getKey();
            CompletableFuture<SendResult<UUID, RetryableTaskDTO>> future = entry.getValue();
            try {
                future.join();
                successIds.add(task.getId());
            } catch (CompletionException e) {
                log.error("Ошибка отправки задачи {}: {}", task.getId(), e.getCause(), e);
                self.reschedule(task.getId());
            }
        }
        return successIds;
    }

    public void processRetryableTasks(List<RetryableTaskDTO> tasks, String topic) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = kafkaProducerService.sendTasksToKafka(tasks, topic);
        List<UUID> successIds = awaitAndCollectSuccessfulIds(futureMap);
        if(!successIds.isEmpty()) {
            self.updateStatusByIds(successIds, RetryableTaskStatus.SEND_TO_KAFKA, RetryableTaskStatus.PENDING);
        }
    }
}

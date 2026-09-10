package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RetryableTaskStatusService taskStatusService;

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
        List<RetryableTask> retryableTasks = retryableTaskRepository.findRetryableTasks(type, currentTime, RetryableTaskStatus.PENDING, pageable);

        for (RetryableTask task : retryableTasks) {
            task.setLeaseToken(UUID.randomUUID());
            task.setLeaseExpiresAt(currentTime.plus(Duration.ofSeconds(retryableTaskProperties.getProcessingLeaseSeconds())));
        }
        retryableTaskRepository.saveAll(retryableTasks);
        return retryableTasks.stream().map(retryableTaskMapper::toRetryableTaskDTO).toList();
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
                taskStatusService.reschedule(task.getId(), task.getLeaseToken());
            }
        }
        return successIds;
    }

    public void processRetryableTasks(List<RetryableTaskDTO> tasks, String topic) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = kafkaProducerService.sendTasksToKafka(tasks, topic);
        List<UUID> successIds = awaitAndCollectSuccessfulIds(futureMap);
        if (!successIds.isEmpty()) {
            for (RetryableTaskDTO task : tasks) {
                if (successIds.contains(task.getId())) {
                    taskStatusService.updateStatusById(task.getId(), task.getLeaseToken(), RetryableTaskStatus.SEND_TO_KAFKA, RetryableTaskStatus.PENDING);
                }
            }
        }
    }

    @Transactional
    public void updateStatusByIdWithoutLease(UUID id, RetryableTaskStatus status, RetryableTaskStatus expectedStatus) {
        int updated = retryableTaskRepository.updateStatusByIdWithoutLease(id, status, expectedStatus);
        if (updated == 0) {
            log.warn("Не удалось обновить статус задачи {} (без проверки leaseToken): статус не совпадает", id);
        } else {
            log.debug("Статус задачи {} обновлён на {} (без проверки leaseToken)", id, status);
        }
    }
}
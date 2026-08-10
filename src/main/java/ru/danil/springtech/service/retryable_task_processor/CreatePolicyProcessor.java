package ru.danil.springtech.service.retryable_task_processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.danil.springtech.config.KafkaConfigProperties;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.service.RetryableTaskService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePolicyProcessor implements RetryableTaskProcessor{
    private final KafkaTemplate<UUID, RetryableTaskDTO> kafkaTemplate;
    private final RetryableTaskService retryableTaskService;
    private final KafkaConfigProperties properties;

    @Override
    public void processRetryableTasks(List<RetryableTaskDTO> tasks) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = sendTasksToKafka(tasks);
        List<UUID> successIds = awaitAndCollectSuccessfulIds(futureMap);
        if(!successIds.isEmpty()) {
            retryableTaskService.updateStatusByIds(successIds, RetryableTaskStatus.SEND_TO_KAFKA, RetryableTaskStatus.PENDING);
        }
    }

    @Override
    public RetryableTaskType getSupportedType() {
        return RetryableTaskType.CREATED_MEDICINE_POLICY;
    }

    private Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> sendTasksToKafka(List<RetryableTaskDTO> tasks) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = new LinkedHashMap<>();
        for(RetryableTaskDTO task : tasks) {
            CompletableFuture<SendResult<UUID, RetryableTaskDTO>> future = kafkaTemplate.send(properties.getPolicyCreatedTopicName(),task.getId(), task);
            futureMap.put(task, future);
        }
        return futureMap;
    }

    private void rescheduleTask(RetryableTaskDTO taskDTO) {
        try {
            retryableTaskService.reschedule(taskDTO.getId());
            log.warn("Не удалось отправить задачу {}. Она будет перепланирована.", taskDTO);
        } catch (Exception e) {
            log.error("Не удалось перепланировать задачу {}: {}", taskDTO.getId(), e.getMessage(), e);
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
                rescheduleTask(task);
            }
        }
        return successIds;
    }
}

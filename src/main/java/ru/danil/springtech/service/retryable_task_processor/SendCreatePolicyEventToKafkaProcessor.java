package ru.danil.springtech.service.retryable_task_processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.service.RetryableTaskService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendCreatePolicyEventToKafkaProcessor implements RetryableTaskProcessor{
    private final KafkaTemplate<UUID, RetryableTaskDTO> kafkaTemplate;
    private final RetryableTaskService retryableTaskService;

    @Override
    public void processRetryableTasks(List<RetryableTaskDTO> tasks) {
        try {
            Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = sendTasksToKafka(tasks);
            List<UUID> successIds = collectSuccessfulTaskIds(futureMap);
            if(!successIds.isEmpty()) {
                retryableTaskService.updateStatusByIds(successIds, RetryableTaskStatus.SEND_TO_KAFKA, RetryableTaskStatus.PENDING);
            }
        } catch (Exception e) {
            tasks.forEach(task -> retryableTaskService.reschedule(task.getId()));
        }
    }

    @Override
    public RetryableTaskType getSupportedType() {
        return RetryableTaskType.CREATED_MEDICINE_POLICY;
    }

    private Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> sendTasksToKafka(List<RetryableTaskDTO> tasks) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = new LinkedHashMap<>();
        for(RetryableTaskDTO task : tasks) {
            CompletableFuture<SendResult<UUID, RetryableTaskDTO>> future = kafkaTemplate.send("policy-created-topic",task.getId(), task);
            futureMap.put(task, future);
        }
        kafkaTemplate.flush();
        return futureMap;
    }


    private List<UUID> collectSuccessfulTaskIds(Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap) {
        List<UUID> ids = new ArrayList<>();
        for(var entry : futureMap.entrySet()) {
            addIfSuccess(entry.getValue(), ids, entry.getKey());
        }
        return ids;
    }

    private void addIfSuccess(CompletableFuture<SendResult<UUID, RetryableTaskDTO>> result, List<UUID> ids, RetryableTaskDTO retryableTaskDTO) {
        UUID retryableTaskId = retryableTaskDTO.getId();
        if(!result.isCompletedExceptionally()) {
            ids.add(retryableTaskId);
        } else {
            retryableTaskService.reschedule(retryableTaskId);
            log.warn("Не удалось отправить задачу {}", retryableTaskDTO);
        }
    }
}

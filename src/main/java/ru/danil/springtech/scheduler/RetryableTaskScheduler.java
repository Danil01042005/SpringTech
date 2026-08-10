package ru.danil.springtech.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.service.RetryableTaskService;
import ru.danil.springtech.service.retryable_task_processor.RetryableTaskProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RetryableTaskScheduler {
    private final RetryableTaskService retryableTaskService;
    private final Map<RetryableTaskType, RetryableTaskProcessor> processorByType;

    public RetryableTaskScheduler(RetryableTaskService retryableTaskService,
                                  List<RetryableTaskProcessor> processors) {
        this.retryableTaskService = retryableTaskService;
        this.processorByType = processors.stream().collect(Collectors.toMap(RetryableTaskProcessor::getSupportedType, p -> p));
    }

    @Scheduled(fixedRateString = "${retryable-task-scheduler.fixed-rate}")
    public void executeRetryableTasks() {
        for (var entry : processorByType.entrySet()) {
            var taskType = entry.getKey();
            var processor = entry.getValue();
            var tasks = retryableTaskService.getRetryableTasks(taskType);
            if (tasks.isEmpty()) {
                log.info("Нет задач для выполнения для типа : {}", taskType);
                continue;
            }
            try {
                processor.processRetryableTasks(tasks);
            } catch (Exception e) {
                log.error("Ошибка при обработке задач типа {}: {}", taskType, e.getMessage(), e);
            }
        }
    }
}
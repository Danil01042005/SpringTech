package ru.danil.springtech.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.danil.springtech.config.KafkaConfigProperties;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.service.RetryableTaskService;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryableTaskScheduler {
    private final RetryableTaskService retryableTaskService;
    private final KafkaConfigProperties kafkaConfigProperties;

    @Scheduled(fixedRateString = "${retryable-task-scheduler.fixed-rate}")
    public void executeRetryableTasks() {
        for (RetryableTaskType type : RetryableTaskType.values()) {
            String topic = resolveTopic(type);
            var tasks = retryableTaskService.getRetryableTasks(type);
            if (tasks.isEmpty()) {
                log.info("Нет задач для выполнения для типа : {}", type);
                continue;
            }
            try {
                retryableTaskService.processRetryableTasks(tasks, topic);
            } catch (Exception e) {
                log.error("Ошибка при обработке задач типа {}: {}", type, e.getMessage(), e);
            }
        }
    }

    private String resolveTopic(RetryableTaskType type) {
        if (type == RetryableTaskType.CREATED_MEDICINE_POLICY) {
            return kafkaConfigProperties.getPolicyCreatedTopicName();
        }
        throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
    }
}
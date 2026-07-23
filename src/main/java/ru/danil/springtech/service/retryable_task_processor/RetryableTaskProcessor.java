package ru.danil.springtech.service.retryable_task_processor;

import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;

import java.util.List;

public interface RetryableTaskProcessor {
    void processRetryableTasks(List<RetryableTaskDTO> retryableTasksDTO);
    RetryableTaskType getSupportedType();

}

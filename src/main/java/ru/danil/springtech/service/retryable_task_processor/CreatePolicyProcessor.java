package ru.danil.springtech.service.retryable_task_processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.config.KafkaConfigProperties;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.service.RetryableTaskService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreatePolicyProcessor implements RetryableTaskProcessor{
    private final RetryableTaskService retryableTaskService;
    private final KafkaConfigProperties kafkaConfigProperties;

    @Override
    public void processRetryableTasks(List<RetryableTaskDTO> tasks) {
        retryableTaskService.processRetryableTasks(tasks, kafkaConfigProperties.getPolicyCreatedTopicName());
    }

    @Override
    public RetryableTaskType getSupportedType() {
        return RetryableTaskType.CREATED_MEDICINE_POLICY;
    }

}

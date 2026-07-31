package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
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
            retryableTask.setRetryTime(currentTime.plus(Duration.ofSeconds(properties.getTimeoutInSecond())));
        }
        return retryableTasks.stream().map(retryableTaskMapper::toRetryableTaskDTO).toList();
    }

    @Transactional
    public void updateStatusByIds(List<UUID> ids, RetryableTaskStatus status) {
        retryableTaskRepository.updateStatusByIds(ids, status);
    }

    @Transactional
    public void updateStatusById(UUID id, RetryableTaskStatus status) {
        retryableTaskRepository.updateStatusById(id, status);
    }
}

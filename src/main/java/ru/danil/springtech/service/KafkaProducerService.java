package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<UUID, RetryableTaskDTO> kafkaTemplate;

    public Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> sendTasksToKafka(List<RetryableTaskDTO> tasks, String topic) {
        Map<RetryableTaskDTO, CompletableFuture<SendResult<UUID, RetryableTaskDTO>>> futureMap = new LinkedHashMap<>();
        for(RetryableTaskDTO task : tasks) {
            futureMap.put(task, sendTask(task, topic));
        }
        return futureMap;
    }

    private CompletableFuture<SendResult<UUID, RetryableTaskDTO>> sendTask(RetryableTaskDTO task, String topic) {
        try {
            return kafkaTemplate.send(topic,task.getId(), task);
        } catch (KafkaException e) {
            log.error("Не удалось отправить задачу {} в Kafka", task.getId(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}

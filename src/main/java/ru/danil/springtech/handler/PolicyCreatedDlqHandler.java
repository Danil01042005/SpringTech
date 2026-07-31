package ru.danil.springtech.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.mapper.RetryableTaskMapper;
import ru.danil.springtech.service.ActorService;
import ru.danil.springtech.service.RetryableTaskService;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PolicyCreatedDlqHandler {
    private final RetryableTaskMapper retryableTaskMapper;
    private final RetryableTaskService retryableTaskService;
    private final ActorService actorService;

    @KafkaListener(topics = "${kafka-listener.topic-name-in-policy-created-dlq-handler}")
    public void handle(List<RetryableTaskDTO> retryableTaskDTOS, Acknowledgment ack) {
    for (RetryableTaskDTO retryableTaskDTO : retryableTaskDTOS) {
        try {
            PolicyDTO policyDTO = retryableTaskMapper.toPolicyDTOFromPayloadOfRetryableTask(retryableTaskDTO);
            UUID actorId = policyDTO.getActorId();
            actorService.deleteActorById(actorId);
            retryableTaskService.updateStatusById(retryableTaskDTO.getId(), RetryableTaskStatus.DELETE_ACTOR_COMPENSATED);
        } catch (Exception e) {
            log.error("Ошибка компенсации задачи {}", retryableTaskDTO.getId(), e);
        }
    }
    ack.acknowledge();
    }
}
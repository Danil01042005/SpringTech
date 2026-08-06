package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;

@RequiredArgsConstructor
@Service
@Slf4j
public class ActorCreationService {
    private final ActorService actorService;
    private final RetryableTaskService retryableTaskService;

    @Transactional
    public ActorDTO createActor(ActorDTO actorDTO) {
        ActorDTO saveCandidate = new ActorDTO();
        BeanUtils.copyProperties(actorDTO, saveCandidate);
        if (actorDTO.getPolicy() != null) {
            saveCandidate.setPolicyStatus(PolicyStatus.IN_PROGRESS);
        }
        ActorDTO savedActorDTO = actorService.createActor(saveCandidate);
        createRetryableTaskIfNeeded(saveCandidate);
        return savedActorDTO;
    }

    private void createRetryableTaskIfNeeded(ActorDTO actorDTO) {
        if (actorDTO.getPolicy() != null) {
            retryableTaskService.createRetryableTask(actorDTO.getPolicy(), RetryableTaskType.CREATED_MEDICINE_POLICY);
        }
    }
}

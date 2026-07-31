package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;

@RequiredArgsConstructor
@Service
@Slf4j
public class ActorOutboxService {
    private final ActorService actorService;
    private final RetryableTaskService retryableTaskService;

    @Transactional
    public ActorDTO createActorAndRetryableTask(ActorDTO request) {
        request.setPolicyStatus(PolicyStatus.IN_PROGRESS);
        ActorDTO actorDTO = actorService.createActor(request);
        PolicyDTO policyDTO = request.getPolicy();
        if(policyDTO != null) {
            policyDTO.setActorId(actorDTO.getId());
            retryableTaskService.createRetryableTask(policyDTO, RetryableTaskType.CREATED_MEDICINE_POLICY);
        }
        return actorDTO;
    }
}

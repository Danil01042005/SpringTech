package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.mapper.ActorMapper;

@RequiredArgsConstructor
@Service
@Slf4j
public class ActorCreationService {
    private final ActorService actorService;
    private final RetryableTaskService retryableTaskService;
    private final ActorMapper actorMapper;

    private ActorDTO createActor(ActorDTO actorDTO) {
        ActorDTO saveCandidate = actorMapper.toActorDTO(actorMapper.toActor(actorDTO));
        saveCandidate.setPolicyStatus(PolicyStatus.IN_PROGRESS);
        ActorDTO savedActorDTO = actorService.createActor(saveCandidate);
        retryableTaskService.createRetryableTask(saveCandidate.getPolicy(), RetryableTaskType.CREATED_MEDICINE_POLICY);
        return savedActorDTO;
    }

    @Transactional
    public ActorDTO create(ActorDTO request) {
        return switch (request){
            case ActorDTO a when a.getPolicy() != null -> createActor(request);
            default -> actorService.createActor(request);
        };
    }
}

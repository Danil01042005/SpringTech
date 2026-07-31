package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.dto.PersonDTO;

@Slf4j
@RequiredArgsConstructor
@Service
public class ActorCoordinator {
    private final ActorOutboxService actorOutboxService;
    private final ActorService actorService;

    public ActorDTO create(ActorDTO request) {
        return switch (request){
            case ActorDTO a when a.getPolicy() != null -> actorOutboxService.createActorAndRetryableTask(request);
            default -> actorService.createActor(request);
        };
    }

}

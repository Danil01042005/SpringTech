package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.PersonDTO;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonCoordinator {
    private final PersonSagaOrchestrator personSagaOrchestrator;
    private final PersonOutboxService personOutboxService;

    public PersonDTO create(PersonDTO request) {
        PersonDTO localPersonDTO = personOutboxService.createPersonAndRetryableTask(request);
        return switch (request){
            case PersonDTO p when p.getPolicy() != null -> personSagaOrchestrator.createPersonWithPolicy(request, localPersonDTO);
            default -> localPersonDTO;
        };
    }
}

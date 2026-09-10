package ru.danil.springtech.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtech.api.PersonAndPassportApi;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.service.PersonSagaOrchestrator;
import ru.danil.springtech.service.PersonService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class PersonController implements PersonAndPassportApi {
    private final PersonService personService;
    private final PersonSagaOrchestrator personSagaOrchestrator;

    @Override
    public ResponseEntity<PersonDTO> createPerson(@Valid PersonDTO personDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personSagaOrchestrator.create(personDTO));
    }

    @Override
    public ResponseEntity<Void> deletePerson(UUID id) {
        personService.deletePersonById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PersonDTO> getPerson(UUID id) {
        return ResponseEntity.ok(personSagaOrchestrator.getPerson(id));
    }

    @Override
    public ResponseEntity<PersonDTO> personUpdate(UUID id, @Valid PersonDTO updatedPersonDTO) {
        return ResponseEntity.ok(personService.updatePerson(id , updatedPersonDTO));
    }
}
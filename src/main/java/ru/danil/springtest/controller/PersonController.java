package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtest.api.PersonAndPassportApi;
import ru.danil.springtest.dto.PersonDTO;
import ru.danil.springtest.mapper.PersonMapper;
import ru.danil.springtest.service.PersonService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
public class PersonController implements PersonAndPassportApi {
    private final PersonService personService;
    private final PersonMapper personMapper;

    @Override
    public ResponseEntity<PersonDTO> createPerson(@Valid PersonDTO personDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(personMapper.toPersonDTO(personService.createPerson(personMapper.toPerson(personDTO))));
    }

    @Override
    public ResponseEntity<Void> deletePerson(UUID id) {
        personService.deletePersonById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PersonDTO> getPerson(UUID id) {
        return ResponseEntity.ok(personMapper.toPersonDTO(personService.getPerson(id)));
    }

    @Override
    public ResponseEntity<PersonDTO> personUpdate(UUID id, @Valid PersonDTO personDTO) {
        return ResponseEntity.ok(personMapper.toPersonDTO(personService.updatePerson(id , personMapper.toPerson(personDTO))));
    }
}

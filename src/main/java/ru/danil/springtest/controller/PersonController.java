package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtest.api.PersonAndPassportApi;
import ru.danil.springtest.dto.PersonDTO;
import ru.danil.springtest.model.Person;
import ru.danil.springtest.service.PersonService;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class PersonController implements PersonAndPassportApi {

    private final ModelMapper modelMapper;
    private final PersonService personService;

    @Override
    public ResponseEntity<PersonDTO> createPerson(@Valid PersonDTO personDTO) {
        Person person = personService.createPerson(convertToPerson(personDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(converToPersonDTO(person));
    }

    @Override
    public ResponseEntity<Void> deletePerson(UUID id) {
        personService.deletePersonById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PersonDTO> getPerson(UUID id) {
        return ResponseEntity.ok(converToPersonDTO(personService.getPerson(id)));
    }

    @Override
    public ResponseEntity<PersonDTO> personUpdate(UUID id, @Valid PersonDTO personDTO) {
        return ResponseEntity.ok().body(converToPersonDTO(personService.personUpdate(id, convertToPerson(personDTO))));
    }

    private Person convertToPerson(PersonDTO personDTO){
        return modelMapper.map(personDTO, Person.class);
    }

    private PersonDTO converToPersonDTO(Person person) {
        return modelMapper.map(person, PersonDTO.class);
    }
}

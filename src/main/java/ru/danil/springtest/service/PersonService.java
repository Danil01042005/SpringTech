package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.dto.PersonDTO;
import ru.danil.springtest.mapper.PersonMapper;
import ru.danil.springtest.model.Person;
import ru.danil.springtest.repository.PersonRepository;
import ru.danil.springtest.exeption.ObjectNotFound;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Transactional
    public PersonDTO createPerson(PersonDTO personDTO) {
        return personMapper.toPersonDTO(personRepository.save(personMapper.toPerson(personDTO)));
    }

    @Transactional(readOnly = true)
    public PersonDTO getPerson(UUID id) {
        return personMapper.toPersonDTO(personRepository.findByIdWithPassport(id).orElseThrow(() -> {
            log.error("Человек с таким айди не найде {}", id);
            return new ObjectNotFound("Человек с таким айди не найден " + id);
        }));
    }

    @Transactional
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    public PersonDTO updatePerson(UUID id, PersonDTO updatedPersonDTO) {
        Person person = personMapper.toPerson(getPerson(id));
        personMapper.updatePerson(updatedPersonDTO, person);
        return personMapper.toPersonDTO(person);
    }
}

package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.mapper.PersonMapper;
import ru.danil.springtech.model.Person;
import ru.danil.springtech.repository.PersonRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Transactional
    @CachePut(value = "PERSON_CACHE", key = "#result.id")
    public PersonDTO createPersonLocal(PersonDTO personDTO) {
        return personMapper.toPersonDTO(personRepository.save(personMapper.toPerson(personDTO)));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "PERSON_CACHE", key = "#id")
    public PersonDTO getLocalPerson(UUID id) {
        Person person = personRepository.findPersonById(id).orElseThrow(() -> {
            log.error("Человек с таким айди не найде {}", id);
            throw new ObjectNotFoundException("Человек с таким айди не найден " + id);
        });

        log.debug("Найден человек: {}, passportId={}",
                person.toString(), person.getPassport() != null ? person.getPassport().getId() : null);
        return personMapper.toPersonDTO(person);
    }

    @Transactional
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    public PersonDTO updatePerson(UUID id, PersonDTO updatedPersonDTO) {
        Person person = personMapper.toPerson(getLocalPerson(id));
        personMapper.updatePerson(updatedPersonDTO, person);
        return personMapper.toPersonDTO(personRepository.save(person));
    }
}

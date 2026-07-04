package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.mapper.PersonMapper;
import ru.danil.springtech.model.Person;
import ru.danil.springtech.repository.PersonRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {
    static final String PERSON_CACHE = "PERSON_CACHE";

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @Transactional
    public PersonDTO createPerson(PersonDTO personDTO) {
        Person person = personMapper.toPerson(personDTO);
        return personMapper.toPersonDTO(personRepository.save(person));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = PERSON_CACHE, key = "#id")
    public PersonDTO getLocalPerson(UUID id) {
        Person person = returnPersonOrThrow(personRepository.findPersonById(id), id);
        log.debug("Найден человек: {}", person.toString());
        return personMapper.toPersonDTO(person);
    }

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#id")
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#id")
    public PersonDTO updatePerson(UUID id, PersonDTO updatedPersonDTO) {
        Person person = returnPersonOrThrow(personRepository.findPersonById(id), id);
        personMapper.updatePerson(updatedPersonDTO, person);
        return personMapper.toPersonDTO(person);
    }

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#id")
    public PersonDTO updatePolicyStatus(UUID id, PolicyStatus policyStatus) {
        Person person = personRepository.getReferenceById(id);
        person.setPolicyStatus(policyStatus);
        return personMapper.toPersonDTO(person);
    }

    public void setPolicyStatusInPerson(UUID id, PolicyStatus policyStatus) {
        personRepository.getReferenceById(id).setPolicyStatus(policyStatus);
    }

    private Person returnPersonOrThrow(Optional<Person> person, UUID personId) {
        return person.orElseThrow(() -> {
            log.error("Человек с таким айди не найден {}", personId);
            return new ObjectNotFoundException("Человек с таким айди не найден " + personId);
        });
    }

    public PersonDTO attachPolicyToPerson(PersonDTO personDTO, PolicyDTO policyDTO) {
        personDTO.setPolicy(policyDTO);
        return personDTO;
    }
}

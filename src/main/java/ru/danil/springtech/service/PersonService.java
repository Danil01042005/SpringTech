package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.mapper.PersonMapper;
import ru.danil.springtech.mapper.PersonPolicyStatusMapper;
import ru.danil.springtech.model.Person;
import ru.danil.springtech.model.enums.PersonPolicyStatus;
import ru.danil.springtech.repository.PersonRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {
    static final String PERSON_CACHE = "PERSON_CACHE";

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PersonPolicyStatusMapper personPolicyStatusMapper;

    @Transactional
    @CachePut(value = PERSON_CACHE, key = "#result.id")
    public PersonDTO createPersonLocal(PersonDTO personDTO) {
        Person person = personMapper.toPerson(personDTO);
        return personMapper.toPersonDTO(personRepository.save(person));
    }

    @Transactional
    @CachePut(value = PERSON_CACHE, key = "#result.id")
    public PersonDTO createPersonLocalWithPolicy(PersonDTO personDTO) {
        Person person = personMapper.toPerson(personDTO);
        person.setPolicyStatus(PersonPolicyStatus.PENDING);
        return personMapper.toPersonDTO(personRepository.save(person));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = PERSON_CACHE, key = "#id")
    public PersonDTO getLocalPerson(UUID id) {
        Person person = personRepository.findPersonById(id).orElseThrow(() -> {
            log.error("Человек с таким айди не найден {}", id);
            return new ObjectNotFoundException("Человек с таким айди не найден " + id);
        });
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
        Person person = personRepository.findPersonById(id).orElseThrow(() -> {
            log.error("Человек с таким айди не найден {}", id);
            return new ObjectNotFoundException("Человек с таким айди не найден " + id);
        });
        personMapper.updatePerson(updatedPersonDTO, person);
        return personMapper.toPersonDTO(person);
    }

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#id")
    public void updatePolicyStatus(UUID id, PersonPolicyStatus policyStatus) {
        setPolicyStatus(id, policyStatus);
    }

    public PersonDTO enrichPolicy(PersonDTO personDTO, PolicyDTO policy) {
        personDTO.setPolicy(policy);
        return personDTO;
    }

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#personDTO.id")
    public PersonDTO attachPolicy(PersonDTO personDTO, PolicyDTO policy, PersonPolicyStatus policyStatus) {
        setPolicyStatus(personDTO.getId(), policyStatus);
        personDTO.setPolicy(policy);
        personDTO.setPolicyStatus(personPolicyStatusMapper.toDto(policyStatus));
        return personDTO;
    }

    private void setPolicyStatus(UUID id, PersonPolicyStatus policyStatus) {
        personRepository.getReferenceById(id).setPolicyStatus(policyStatus);
    }
}

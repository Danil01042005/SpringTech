package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.client.PolicyClient;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.mapper.PersonMapper;
import ru.danil.springtech.model.Person;
import ru.danil.springtech.repository.PersonRepository;
import ru.danil.springtech.exсeption.ObjectNotFoundException;
import ru.danil.springtech.util.RetryBudget;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final PolicyClient policyClient;
    private final RetryBudget retryBudget;

    @Lazy
    @Autowired
    private PersonService self;

    @CachePut(value = "PERSON_CACHE", key = "#result.id")
    public PersonDTO createPerson(PersonDTO newPersonDTO) {
        PersonDTO personDTO = self.createPersonLocal(newPersonDTO);
        if(newPersonDTO.getPolicy() != null) {
            PolicyDTO newPolicyDTO = self.createPolicy(personDTO.getId(), newPersonDTO.getPolicy());
            personDTO.setPolicy(newPolicyDTO);
        }
        return personDTO;
    }

    @Retryable(retryFor = FeignException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 100 , multiplier = 2.0, random = true),
            exceptionExpression = "@retryBudget.retry()"
    )
    public PolicyDTO createPolicy(UUID personId, PolicyDTO policyDTO) {
        try {
            policyDTO.setPersonId(personId);
            PolicyDTO newPolicyDTO = policyClient.createPolicyDTO(policyDTO);
            retryBudget.successRequest();
            log.debug("Полис {} создан успешно и привязан к человеку {}" , newPolicyDTO.getPolicyNumber() , newPolicyDTO.getPersonId());
            return newPolicyDTO;
        } catch (FeignException e) {
            log.error("Сбой сети или ошибка внешнего сервиса медицины при создании полиса: {}, {}", e.getMessage(), policyDTO.getPolicyNumber());
            throw e;
        }
    }

    @Transactional
    public PersonDTO createPersonLocal(PersonDTO personDTO) {
        return personMapper.toPersonDTO(personRepository.save(personMapper.toPerson(personDTO)));
    }

    @Cacheable(value = "PERSON_CACHE", key = "#id")
    public PersonDTO getPerson(UUID id){
        PersonDTO personDTO = self.getLocalPerson(id);
        try {
            PolicyDTO policyDTO = policyClient.getPolicyDTO(id);
            personDTO.setPolicy(policyDTO);
            log.debug("Найден полис с айди {}, номер полиса: {}", id , policyDTO.getPolicyNumber());
            return personDTO;
        } catch (FeignException.NotFound e) {
            log.error("Полис с таким айди не найден: {}, код ответа: {}", id, e.status());
        } catch (FeignException e) {
            log.error("Сбой сети или ошибка внешнего сервиса медицины при запросе ID {}: {}", id, e.getMessage());
        }
        return personDTO;
    }

    @Transactional(readOnly = true)
    public PersonDTO getLocalPerson(UUID id) {
        Person person = personRepository.findByIdWithPassport(id).orElseThrow(() -> {
            log.error("Человек с таким айди не найде {}", id);
            return new ObjectNotFoundException("Человек с таким айди не найден " + id);
        });

        log.debug("Найден человек: {}, passportId={}",
                person.toString(), person.getPassport() != null ? person.getPassport().getId() : null);
        return personMapper.toPersonDTO(person);
    }

    @Transactional
//    @CacheEvict(value = "PERSON_CACHE", key = "#id")
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
//    @CachePut(value = "PERSON_CACHE", key = "#result.id")
    public PersonDTO updatePerson(UUID id, PersonDTO updatedPersonDTO) {
        Person person = personMapper.toPerson(getPerson(id));
        personMapper.updatePerson(updatedPersonDTO, person);
        return personMapper.toPersonDTO(person);
    }
}

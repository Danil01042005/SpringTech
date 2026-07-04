package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;

import static ru.danil.springtech.service.PersonService.PERSON_CACHE;

@Service
@RequiredArgsConstructor
public class PersonPolicyService {
    private final PersonService personService;

    @Transactional
    @CacheEvict(value = PERSON_CACHE, key = "#personDTO.id")
    public PersonDTO attachPolicy(PersonDTO personDTO, PolicyDTO policyDTO, PolicyStatus policyStatus) {
        personDTO.setPolicy(policyDTO);
        personDTO.setPolicyStatus(policyStatus);
        personService.setPolicyStatusInPerson(personDTO.getId(), policyStatus);
        return personDTO;
    }
}

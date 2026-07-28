package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;

@RequiredArgsConstructor
@Service
@Slf4j
public class PersonOutboxService {
    private final PersonService personService;
    private final RetryableTaskService retryableTaskService;

    @Transactional
    public PersonDTO createPersonAndRetryableTask(PersonDTO request) {
        PersonDTO personDTO = personService.createPerson(request);
        PolicyDTO policyDTO = request.getPolicy();
        if(policyDTO != null) {
            policyDTO.setPersonId(personDTO.getId());
            retryableTaskService.createRetryableTask(policyDTO, RetryableTaskType.CREATED_MEDICINE_POLICY);
        }
        return personDTO;
    }
}

package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.util.job.PolicyBackgroundJob;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonSagaOrchestrator {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final PolicyBackgroundJob policyBackgroundJob;

    private PersonDTO createPersonOnly(PersonDTO personDTO) {
        return personService.createPerson(personDTO);
    }

    private PersonDTO createPersonWithPolicy(PersonDTO newPersonDTO) {
        PersonDTO personDTO = createPersonOnly(newPersonDTO);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPersonDTO.getPolicy());
            log.debug("Полис успешно создан: {}", policyDTO.toString());
            return personService.attachPolicy(personDTO, policyDTO, PolicyStatus.COMPLETED);
        } catch (FeignException e) {
            policyBackgroundJob.scheduleCreatePolicyWithBudget(personDTO, newPersonDTO.getPolicy());
            personDTO.setPolicyStatus(PolicyStatus.PENDING);
            personService.updatePolicyStatus(personDTO.getId(), PolicyStatus.PENDING);
            return personDTO;
        }
    }

    public PersonDTO create(PersonDTO personDTO) {
        return switch (personDTO){
            case PersonDTO p when p.getPolicy() != null -> createPersonWithPolicy(personDTO);
            default -> createPersonOnly(personDTO);
        };
    }

    public PersonDTO getLocalPersonById(UUID personId) {
        return personService.getLocalPerson(personId);
    }

    public PolicyDTO getPolicyDTO(UUID personId){
        try {
            return medicineIntegrationService.getPolicyById(personId);
        } catch (Exception e) {
            return handleGetQueryFailure(e, personId);
        }
    }

    public PolicyDTO handleGetQueryFailure(Exception e, UUID personId) {
        switch (e) {
            case FeignException.NotFound notFound-> {
                log.debug("Полис для человека {}  не найден, отдаём данные без полиса", personId);
                return null;
            }
            case FeignException f -> {
                log.error("Временная недоступность медицины при запросе полиса для человека: {}", personId);
                return null;
            }
            default -> {
                log.error("Неожиданная ошибка {}", personId);
                return null;
            }
        }
    }

    public PersonDTO getPerson(UUID personId){
        PersonDTO personDTO = getLocalPersonById(personId);
        PolicyDTO policyDTO = getPolicyDTO(personId);
        if(policyDTO != null) {
            return personService.attachPolicy(personDTO, policyDTO);
        }
        return personDTO;
    }
}

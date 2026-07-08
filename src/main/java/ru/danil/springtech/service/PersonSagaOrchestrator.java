package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.ErrorResponse;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.util.job.PolicyBackgroundJob;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonSagaOrchestrator {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final PolicyBackgroundJob policyBackgroundJob;
    private final PersonPolicyService personPolicyService;


    private PersonDTO createPersonWithPolicy(PersonDTO newPersonDTO) {
        PersonDTO personDTO = personService.createPerson(newPersonDTO);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPersonDTO.getPolicy());
            log.debug("Полис успешно создан: {}", policyDTO.toString());
            return personPolicyService.attachPolicy(personDTO, policyDTO, PolicyStatus.COMPLETED);
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
            default -> personService.createPerson(personDTO);
        };
    }

    public PersonDTO getLocalPersonById(UUID personId) {
        return personService.getLocalPerson(personId);
    }

    private PersonDTO handleGetPolicyQueryFailure(Exception e, PersonDTO personDTO) {
        UUID personId = personDTO.getId();
        switch (e) {
            case FeignException.NotFound notFound-> {
                log.debug("Полис для человека {}  не найден, отдаём данные без полиса", personId);
                personDTO.setPolicyStatus(PolicyStatus.NOT_FOUND);
                return personDTO;
            }
            case FeignException f -> {
                log.error("Временная недоступность медицины при запросе полиса для человека: {}", personId);
                return createErrorIntegrationResponseInPerson("Временная недоступность медицины", personDTO);
            }
            default -> {
                log.error("Неожиданная ошибка при запросе полиса для {}", personId, e);
                return createErrorIntegrationResponseInPerson("Внутренняя ошибка сервера", personDTO);
            }
        }
    }

    private PersonDTO createErrorIntegrationResponseInPerson(String message, PersonDTO personDTO) {
        ErrorResponse errorResponse = new ErrorResponse().message(message);
        personDTO.setMedicineErrorResponse(errorResponse);
        return personDTO;
    }

    public PersonDTO getPerson(UUID personId){
        PersonDTO personDTO = getLocalPersonById(personId);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.getPolicyById(personId);
            personDTO.setPolicy(policyDTO);
            return personService.attachPolicyToPerson(personDTO, policyDTO);
        } catch (Exception e) {
            return handleGetPolicyQueryFailure(e, personDTO);
        }
    }
}

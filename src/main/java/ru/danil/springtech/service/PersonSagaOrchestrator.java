package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.ErrorResponse;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.util.job.PersonBackgroundJob;
import ru.danil.springtech.util.job.PolicyBackgroundJob;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonSagaOrchestrator {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final PolicyBackgroundJob policyBackgroundJob;
    private final PersonPolicyService personPolicyService;
    private final PersonBackgroundJob personBackgroundJob;


    public PersonDTO createPersonWithPolicy(PersonDTO request , PersonDTO localPersonDTO) {
        try {
            PolicyDTO policyDTO = medicineIntegrationService.createPolicyDTO(localPersonDTO.getId(), request.getPolicy());
            log.debug("Полис успешно создан: {}", policyDTO.toString());
            return personPolicyService.attachPolicy(localPersonDTO, policyDTO, PolicyStatus.COMPLETED);
        } catch (FeignException e) {
            policyBackgroundJob.scheduleCreatePolicyWithBudget(localPersonDTO, request.getPolicy());
            localPersonDTO.setPolicyStatus(PolicyStatus.PENDING);
            personService.updatePolicyStatus(localPersonDTO.getId(), PolicyStatus.PENDING);
            return localPersonDTO;
        }
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
        PersonDTO personDTO = personService.getLocalPerson(personId);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.getPolicyById(personId);
            personDTO.setPolicy(policyDTO);
            return personService.attachPolicyToPerson(personDTO, policyDTO);
        } catch (Exception e) {
            return handleGetPolicyQueryFailure(e, personDTO);
        }
    }

    public void compensateDeleteLocalPerson(UUID personId) {
        try {
            personService.deletePersonById(personId);
            log.debug("Успешно удален человек: {}", personId);
        } catch (Exception e) {
            log.warn("Компенсация не удалась для человека {}, создаем джобу: {}", personId, e.getMessage());
            personBackgroundJob.compensateDeleteLocalPersonHandleFailure(personId);
        }
    }
}
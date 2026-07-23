package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.dto.ErrorResponse;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.model.PersonWithOutbox;
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
    private final RetryableTaskService retryableTaskService;
    private final PersonBackgroundJob personBackgroundJob;


    private PersonDTO createPersonWithPolicy(PersonDTO newPersonDTO) {
        PersonWithOutbox personWithOutbox = createPersonAndRetryableTaskLocal(newPersonDTO);
        PersonDTO personDTO = personWithOutbox.personDTO();
        RetryableTaskDTO retryableTaskDTO = personWithOutbox.retryableTaskDTO();
        try {
            PolicyDTO policyDTO = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPersonDTO.getPolicy());
            log.debug("Полис успешно создан: {}", policyDTO.toString());
            return personPolicyService.attachPolicy(personDTO, policyDTO, PolicyStatus.COMPLETED);
        } catch (FeignException e) {
            policyBackgroundJob.scheduleCreatePolicyWithBudget(personDTO, newPersonDTO.getPolicy(), retryableTaskDTO.getId());
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

    @Transactional
    private PersonWithOutbox createPersonAndRetryableTaskLocal(PersonDTO newPersonDTO) {
        PersonDTO personDTO = personService.createPerson(newPersonDTO);
        RetryableTaskDTO retryableTaskDTO = retryableTaskService.createRetryableTask(personDTO.getPolicy(), RetryableTaskType.CREATED_MEDICINE_POLICY);
        return new PersonWithOutbox(retryableTaskDTO, personDTO);
    }
}

package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.config.RetryBudgetConfig;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.model.enums.PersonPolicyStatus;
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
    private final PersonBackgroundJob personBackgroundJob;
    private final RetryBudgetConfig retryBudgetConfig;

    public PersonDTO createPerson(PersonDTO newPersonDTO) {
        if (newPersonDTO.getPolicy() == null) {
            return personService.createPersonLocal(newPersonDTO);
        }

        PersonDTO personDTO = personService.createPersonLocalWithPolicy(newPersonDTO);
        PolicyDTO newPolicyDTO = newPersonDTO.getPolicy();
        try {
            PolicyDTO policy = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPolicyDTO);
            log.debug("Полис успешно создан: {}", policy);
            return personService.attachPolicy(personDTO, policy, PersonPolicyStatus.COMPLETED);
        } catch (FeignException e) {
            return handlePolicyCreateFailure(personDTO, newPolicyDTO, e, false);
        }
    }

    private PersonDTO handlePolicyCreateFailure(PersonDTO personDTO, PolicyDTO newPolicyDTO, FeignException error, boolean afterTimeout) {
        if (retryBudgetConfig.shouldCompensate(error, afterTimeout)) {
            log.error("Бизнес ошибка при создании полиса для человека {}: статус {}, {}", personDTO.toString(), error.status(), error.getMessage());
            compensateAndThrow(personDTO, "Не удалось создать человека: ошибка данных");
        }
        if (!afterTimeout && error.status() == -1) {
            return handleCreateTimeout(personDTO, newPolicyDTO);
        }
        if (retryBudgetConfig.shouldRetryLater(error)) {
            return schedulePolicyRetry(personDTO, newPolicyDTO, error);
        }
        log.error("Неожиданная ошибка медицины при создании полиса для человека {}: статус {}", personDTO.toString(), error.status());
        throw error;
    }

    private PersonDTO handleCreateTimeout(PersonDTO personDTO, PolicyDTO policyTemplate) {
        UUID personId = personDTO.getId();
        try {
            PolicyDTO policy = medicineIntegrationService.getPolicyByIdDTOWithoutRetry(personId);
            log.debug("Полис создан несмотря на таймаут: {}", policy.getPolicyNumber());
            return personService.attachPolicy(personDTO, policy, PersonPolicyStatus.COMPLETED);
        } catch (FeignException e) {
            return handlePolicyCreateFailure(personDTO, policyTemplate, e, true);
        }
    }

    private PersonDTO schedulePolicyRetry(PersonDTO personDTO, PolicyDTO newPolicyDTO, FeignException error) {
        log.debug("Техническая ошибка Медицины (статус {}), фоновое создание полиса для человека {}: {}", error.status(), personDTO.toString(), error.getMessage());
        policyBackgroundJob.scheduleCreatePolicyWithBudget(personDTO, newPolicyDTO);
        return personService.enrichPolicy(personDTO, null);
    }

    private void compensateAndThrow(PersonDTO personDTO, String message) {
        personBackgroundJob.compensateDeleteLocalPerson(personDTO);
        throw new ServiceUnavailableException(message);
    }
}

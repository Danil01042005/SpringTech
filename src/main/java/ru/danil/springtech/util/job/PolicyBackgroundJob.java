package ru.danil.springtech.util.job;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.exception.PolicyCreationException;
import ru.danil.springtech.service.RetryBudgetService;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.service.MedicineIntegrationService;
import ru.danil.springtech.service.PersonService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyBackgroundJob {
    private final RetryBudgetService retryBudgetService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final JobScheduler jobScheduler;
    private final PersonService personService;

    public void scheduleCreatePolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        policyDTO.setPersonId(personDTO.getId());
        jobScheduler.schedule(
                Instant.now().plus(30, ChronoUnit.MINUTES),
                () -> createPolicyWithBudget(personDTO, policyDTO)
        );
    }

    @Job(name = "Create policy for person %0")
    public void createPolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        UUID personId = personDTO.getId();
        try {
            checkBudget(personDTO, policyDTO);
            medicineIntegrationService.createPolicyWithoutRetry(personId, policyDTO);
            retryBudgetService.successRequest();
            personService.updatePolicyStatus(personId, PolicyStatus.COMPLETED);
            log.debug("Фоновое создание полиса успешно для человека {}", personDTO.toString());
        } catch (Exception e) {
            handleFailure(e, personId);
        }
    }

    private void checkBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        if (!retryBudgetService.retryScriptExecute()) {
            log.debug("Бюджет токенов исчерпан, откладываем создание полиса для человека {}", personDTO.getId());
            throw new PolicyCreationException("Бюджета токенов не хватило");
        }
    }

    private void handleFailure(Exception e, UUID personId) {
        switch (e) {
            case FeignException f -> {
                log.error("Ошибка MedicineService для {}: статус {}", personId, f.status());
                personService.updatePolicyStatus(personId, PolicyStatus.FAILED);
            }
            case PolicyCreationException p -> {
                log.error("Ошибка создания полиса для {}: {}", personId, p.getMessage());
                personService.updatePolicyStatus(personId, PolicyStatus.FAILED);
            }
            default -> {
                log.error("Неожиданная ошибка при создании полиса для {}", personId);
                personService.updatePolicyStatus(personId, PolicyStatus.FAILED);
            }
        }
    }
}

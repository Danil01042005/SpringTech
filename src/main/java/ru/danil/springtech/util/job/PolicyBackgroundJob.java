package ru.danil.springtech.util.job;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;
import ru.danil.springtech.exception.PolicyCreationException;
import ru.danil.springtech.service.RetryBudgetService;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.service.MedicineIntegrationService;
import ru.danil.springtech.service.PersonService;
import ru.danil.springtech.service.RetryableTaskService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties("policy-background-jobs-config")
public class PolicyBackgroundJob {
    private final RetryBudgetService retryBudgetService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final JobScheduler jobScheduler;
    private final PersonService personService;
    private final PersonBackgroundJob personBackgroundJob;
    private final RetryableTaskService retryableTaskService;
    private int amountToAddMinutes;

    public void scheduleCreatePolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        policyDTO.setPersonId(personDTO.getId());
        jobScheduler.schedule(
                Instant.now().plus(amountToAddMinutes, ChronoUnit.MINUTES),
                () -> createPolicyWithBudget(personDTO, policyDTO, personDTO.getId())
        );
    }

    @Job(name = "Create policy for person %0")
    public void createPolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO, UUID retryableTaskId) {
        UUID personId = personDTO.getId();
        try {
            executePolicyCreation(personDTO, policyDTO, retryableTaskId);
        } catch (Exception e) {
            handleFailure(e, personId, retryableTaskId);
        }
    }

    private void executePolicyCreation(PersonDTO personDTO, PolicyDTO policyDTO, UUID retryableTaskId) {
        UUID personId = personDTO.getId();
        checkBudget(personDTO,policyDTO);
        medicineIntegrationService.createPolicyWithoutRetry(personId, policyDTO);
        handleSuccess(personId, retryableTaskId);
    }

    private void handleSuccess(UUID personId, UUID retryableTaskId) {
        retryBudgetService.successRequest();
        personService.updatePolicyStatus(personId, PolicyStatus.COMPLETED);
        retryableTaskService.updateStatusById(retryableTaskId, RetryableTaskStatus.SUCCESS);
        log.debug("Фоновое создание полиса успешно для человека {}", personId);
    }

    private void checkBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        if (!retryBudgetService.retryBudget()) {
            log.debug("Бюджет токенов исчерпан, откладываем создание полиса для человека {}", personDTO.getId());
            throw new PolicyCreationException("Бюджета токенов не хватило");
        }
    }

    private void handleFailure(Exception e, UUID personId, UUID retryableTaskId) {
        retryableTaskService.updateStatusById(retryableTaskId, RetryableTaskStatus.PENDING);
        switch (e) {
            case FeignException f -> handleFeignException(f, personId);
            case PolicyCreationException p -> handlePolicyCreationException(p, personId);
            default -> handleUnexpectedException(e, personId);
        }
    }

    private void handlePolicyCreationException(PolicyCreationException e, UUID personId) {
        log.error("Ошибка создания полиса для {}: {}", personId, e.getMessage());
    }

    private void handleFeignException(FeignException e, UUID personId){
        log.error("Ошибка MedicineService для {}: статус {}", personId, e.status());
    }

    private void handleUnexpectedException(Exception e, UUID personId) {
        log.error("Неожиданная ошибка при создании полиса для {}", personId, e);
    }
}

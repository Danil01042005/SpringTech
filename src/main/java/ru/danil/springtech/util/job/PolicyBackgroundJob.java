package ru.danil.springtech.util.job;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;
import ru.danil.springtech.config.RetryBudgetConfig;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.CompensationFailedException;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.mapper.EntityDTOJobMapper;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.PersonPolicyStatus;
import ru.danil.springtech.service.DeferredJobService;
import ru.danil.springtech.service.MedicineIntegrationService;
import ru.danil.springtech.service.PersonService;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyBackgroundJob {
    private final RetryBudgetConfig retryBudgetConfig;
    private final MedicineIntegrationService medicineIntegrationService;
    private final PersonBackgroundJob personBackgroundJob;
    private final JobScheduler jobScheduler;
    private final DeferredJobService deferredJobService;
    private final EntityDTOJobMapper entityDTOJobMapper;
    private final PersonService personService;

    public void scheduleCreatePolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        policyDTO.setPersonId(personDTO.getId());
        jobScheduler.schedule(Instant.now(), () -> createPolicyWithBudget(personDTO, policyDTO));
    }

    @Job(name = "Create policy for person %0", retries = 0)
    public void createPolicyWithBudget(PersonDTO personDTO, PolicyDTO policyDTO) {
        UUID personId = personDTO.getId();
        if (!retryBudgetConfig.retryScriptExecute()) {
            log.debug("Бюджет токенов исчерпан, откладываем создание полиса для человека {}", personId);
            persistPolicyCreateDeferredJob(personId, policyDTO);
            throw new CompensationFailedException("Бюджета токенов не хватило");
        }
        try {
            medicineIntegrationService.createPolicyDTOWithoutRetry(personId, policyDTO);
            retryBudgetConfig.successRequest();
            personService.updatePolicyStatus(personId, PersonPolicyStatus.COMPLETED);
            log.debug("Фоновое создание полиса успешно для человека {}", personDTO.toString());
        } catch (FeignException e) {
            handlePolicyCreateFailure(personDTO, policyDTO, e, false);
        }
    }

    private void handlePolicyCreateFailure(PersonDTO personDTO, PolicyDTO policyDTO, FeignException error, boolean afterTimeoutVerify) {
        UUID personId = personDTO.getId();
        if (retryBudgetConfig.shouldCompensate(error, afterTimeoutVerify)) {
            log.error("Бизнес ошибка локальный откат для человека {}: статус {}, {}", personDTO.toString(), error.status(), error.getMessage());
            personBackgroundJob.compensateDeleteLocalPerson(personDTO);
            personService.updatePolicyStatus(personId, PersonPolicyStatus.FAILED);
            return;
        }
        if (!afterTimeoutVerify && error.status() == -1 && resolvePolicyAfterTimeoutInJob(personDTO, policyDTO)) {
            return;
        }
        if (retryBudgetConfig.shouldRetryLater(error)) {
            log.debug("Техническая ошибка Медицины (статус {}), откладываем полис для человека {}: {}", error.status(), personDTO.toString(), error.getMessage());
            persistPolicyCreateDeferredJob(personId, policyDTO);
            throw new ServiceUnavailableException("Сервис медицины не отвечает");
        }
        log.error("Неожиданная ошибка Медицины для человека {}: статус {}", personId, error.status());
        throw error;
    }

    private void persistPolicyCreateDeferredJob(UUID personId, PolicyDTO policyDTO) {
        DeferredJob deferredJob = entityDTOJobMapper.toPolicyJob(personId, policyDTO, ActionName.CREATE);
        deferredJobService.createJob(deferredJob);
    }

    private boolean resolvePolicyAfterTimeoutInJob(PersonDTO personDTO, PolicyDTO policyDTO) {
        UUID personId = personDTO.getId();
        try {
            medicineIntegrationService.getPolicyByIdDTOWithoutRetry(personId);
            retryBudgetConfig.successRequest();
            personService.updatePolicyStatus(personId, PersonPolicyStatus.COMPLETED);
            log.debug("Полис найден после таймаута для человека {}", personId);
            return true;
        } catch (FeignException e) {
            if (retryBudgetConfig.shouldCompensate(e, true)) {
                log.error("Бизнес ошибка после таймаута, локальный откат для человека {}: статус {}, {}", personDTO.toString(), e.status(), e.getMessage());
                personBackgroundJob.compensateDeleteLocalPerson(personDTO);
                personService.updatePolicyStatus(personId, PersonPolicyStatus.FAILED);
                return true;
            }
            log.debug("Снова не уверены создался ли полис после тайм аута {}, статус {}", personId, e.status());
            return false;
        }
    }
}

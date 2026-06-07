package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import ru.danil.springtech.config.RetryBudgetConfig;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.CompensationFailedException;
import ru.danil.springtech.exception.ServiceUnavailableException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyBackgroundJobs {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final RetryBudgetConfig retryBudgetConfig;
    private final JobScheduler jobScheduler;

    public void schedulePolicyCreate(UUID personId, PolicyDTO policyDTO) {
        jobScheduler.schedule(
                Instant.now().plus(10, ChronoUnit.MINUTES),
                () -> createPolicyWithBudget(personId, policyDTO)
        );
    }

    public void compensateDeleteLocalPerson(UUID personId) {
        try {
            personService.deletePersonById(personId);
            log.info("Пользователь с айди {} удален", personId);
        } catch (Exception e) {
            //Если откатиться не получилось, запускаем джобу на удаление
            log.warn("Не удалось удалить пользователя {}, {}", personId, e.getMessage());
            scheduleDeleteLocalPersonJob(personId);
        }
    }

    private void scheduleDeleteLocalPersonJob(UUID personId) {
        jobScheduler.schedule(
                Instant.now().plus(10, ChronoUnit.MINUTES),
                () -> deleteLocalPersonJob(personId)
        );
    }

    //Джобы должны идти через мой бюджет чтобы максимальная разовая нагрузка на сервис была не 3x и тд, а 110%
    //Если исключение вылетает, то джоба идет на ретраи
    @Job(name = "Create policy for person %0")
    public void createPolicyWithBudget(UUID personId, PolicyDTO policyDTO) {
        if (!retryBudgetConfig.retry()) {
            throw new CompensationFailedException("Бюджета токенов не хватило");
        }
        try {
            medicineIntegrationService.createPolicyDTOWithoutRetry(personId, policyDTO);
            retryBudgetConfig.successRequest();
        } catch (FeignException.Forbidden e) {
            log.error("Ошибка данных, делаем локальный откат {}: {}", personId, e.getMessage());
            compensateDeleteLocalPerson(personId);
        } catch (FeignException e) {
            //Если тайм аут, то нужно удостовериться создался ли полис во время джобы,
            // если тру выходи из метода с успехом, статус джобы будет помечен как выполнено
            if (e.status() == -1 && resolvePolicyAfterTimeoutInJob(personId)) {
                return;
            }
            throw new ServiceUnavailableException("Сервис медицины не отвечает");
        }
    }

    @Job(name = "Delete local person %0")
    public void deleteLocalPersonJob(UUID personId) {
        try {
            personService.deletePersonById(personId);
            log.debug("Джоба локального удаления выполнилась с успехом {}", personId);
        } catch (Exception e) {
            log.error("Джоба локального удаления упала {}: {}", personId, e.getMessage());
            throw new CompensationFailedException("Джоба локального удаления не смогла выполниться: " + e.getMessage());
        }
    }

    //Метод который проверяет создался ли полис после джобы которая упала с тайм аутом
    private boolean resolvePolicyAfterTimeoutInJob(UUID personId) {
        try {
            medicineIntegrationService.getPolicyByIdDTOWithoutRetry(personId);
            retryBudgetConfig.successRequest();
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        } catch (FeignException.Forbidden e) {
            compensateDeleteLocalPerson(personId);
            return true;
        } catch (FeignException e) {
            return false;
        }
    }
}

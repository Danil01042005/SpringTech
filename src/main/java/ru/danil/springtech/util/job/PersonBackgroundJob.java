package ru.danil.springtech.util.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.exception.CompensationFailedException;
import ru.danil.springtech.service.PersonService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties("person-background-jobs-config")
public class PersonBackgroundJob {
    private final PersonService personService;
    private final JobScheduler jobScheduler;
    private int amountToAddMinutes;

    @Job(name = "Delete local person %0", retries = 0)
    public void deleteLocalPersonJob(PersonDTO personDTO) {
        try {
            personService.deletePersonById(personDTO.getId());
            log.debug("Джоба локального удаления выполнилась с успехом {}", personDTO.getId());
        } catch (Exception e) {
            log.error("Джоба локального удаления упала {}: {}", personDTO, e.getMessage());
            throw new CompensationFailedException("Джоба локального удаления упала");
        }
    }

    public void compensateDeleteLocalPerson(PersonDTO personDTO) {
        try {
            personService.deletePersonById(personDTO.getId());
            log.debug("Успешно удален человек: {}", personDTO.toString());
        } catch (Exception e) {
            log.warn("Компенсация не удалась для человека {}, создаем джобу: {}", personDTO.toString(), e.getMessage());
            jobScheduler.schedule(Instant.now().plus(amountToAddMinutes, ChronoUnit.MINUTES), () -> deleteLocalPersonJob(personDTO));
        }
    }
}
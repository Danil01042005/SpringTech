package ru.danil.springtech.util.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.mapper.EntityDTOJobMapper;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.service.DeferredJobService;
import ru.danil.springtech.service.PersonService;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class PersonBackgroundJob {
    private final PersonService personService;
    private final EntityDTOJobMapper entityDTOJobMapper;
    private final DeferredJobService deferredJobService;
    private final JobScheduler jobScheduler;

    @Job(name = "Delete local person %0", retries = 0)
    public void deleteLocalPersonJob(PersonDTO personDTO) {
        try {
            personService.deletePersonById(personDTO.getId());
            log.debug("Джоба локального удаления выполнилась с успехом {}", personDTO.getId());
        } catch (Exception e) {
            log.error("Джоба локального удаления упала {}: {}", personDTO, e.getMessage());
            DeferredJob deferredJob = entityDTOJobMapper.toPersonJob(personDTO, ActionName.DELETE);
            deferredJobService.createJob(deferredJob);
            throw e;
        }
    }

    public void compensateDeleteLocalPerson(PersonDTO personDTO) {
        try {
            personService.deletePersonById(personDTO.getId());
            log.debug("Успешно удален человек: {}", personDTO.toString());
        } catch (Exception e) {
            log.warn("Компенсация не удалась для человека {}, создаем джобу: {}", personDTO.toString(), e.getMessage());
            jobScheduler.schedule(Instant.now(), () -> deleteLocalPersonJob(personDTO));
        }
    }
}

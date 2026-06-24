package ru.danil.springtech.service;

import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.repository.PersonRepository;
import ru.danil.springtech.util.job.PersonBackgroundJob;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static ru.danil.springtech.support.PersonTestFixtures.человекБезПолиса;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PolicyBackgroundJobsTest {

    @Autowired
    private PersonBackgroundJob personBackgroundJob;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @MockitoBean
    private JobScheduler jobScheduler;

    @Test
    void компенсацияУдаленияЧеловека_когдаУдалениеУспешно_неПланируетЗадачу() {
        var saved = personService.createPersonLocal(человекБезПолиса("Алексей Ким", 28, "121212"));
        var personDTO = saved;

        personBackgroundJob.compensateDeleteLocalPerson(personDTO);

        assertThat(personRepository.findById(personDTO.getId())).isEmpty();
        verify(jobScheduler, never()).schedule(any(), any(JobLambda.class));
    }

    @Test
    void компенсацияУдаленияЧеловека_когдаУдалениеПадает_планируетФоновуюЗадачу() {
        UUID personId = UUID.randomUUID();
        PersonService failingPersonService = org.mockito.Mockito.mock(PersonService.class);
        doThrow(new RuntimeException("база данных недоступна")).when(failingPersonService).deletePersonById(personId);

        PersonBackgroundJob jobs = new PersonBackgroundJob(
                failingPersonService,
                org.mockito.Mockito.mock(ru.danil.springtech.mapper.EntityDTOJobMapper.class),
                org.mockito.Mockito.mock(DeferredJobService.class),
                jobScheduler
        );

        var personDTO = человекБезПолиса("Тест Тестов", 30, "123456");
        personDTO.setId(personId);
        jobs.compensateDeleteLocalPerson(personDTO);

        verify(jobScheduler).schedule(any(), any(JobLambda.class));
    }
}

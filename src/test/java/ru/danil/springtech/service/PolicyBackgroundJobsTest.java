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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PolicyBackgroundJobsTest {

    @Autowired
    private PolicyBackgroundJobs policyBackgroundJobs;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @MockitoBean
    private JobScheduler jobScheduler;

    @Test
    void compensateDeleteLocalPerson_whenDeleteSucceeds_doesNotScheduleJob() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Алексей Ким", 28, "121212"));
        UUID personId = saved.getId();

        policyBackgroundJobs.compensateDeleteLocalPerson(personId);

        assertThat(personRepository.findById(personId)).isEmpty();
        verify(jobScheduler, never()).schedule(any(), any(JobLambda.class));
    }

    @Test
    void compensateDeleteLocalPerson_whenDeleteFails_schedulesBackgroundJob() {
        UUID personId = UUID.randomUUID();
        PersonService failingPersonService = org.mockito.Mockito.mock(PersonService.class);
        doThrow(new RuntimeException("db unavailable")).when(failingPersonService).deletePersonById(personId);

        PolicyBackgroundJobs jobs = new PolicyBackgroundJobs(
                failingPersonService,
                org.mockito.Mockito.mock(MedicineIntegrationService.class),
                org.mockito.Mockito.mock(ru.danil.springtech.config.RetryBudgetConfig.class),
                jobScheduler
        );

        jobs.compensateDeleteLocalPerson(personId);

        verify(jobScheduler).schedule(any(), any(JobLambda.class));
    }
}

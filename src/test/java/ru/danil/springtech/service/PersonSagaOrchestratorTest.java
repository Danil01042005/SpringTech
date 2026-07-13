package ru.danil.springtech.service;

import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.client.MedicineClient;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.repository.PersonRepository;
import ru.danil.springtech.support.FeignTestExceptions;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.policy;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;
import static ru.danil.springtech.support.PersonTestFixtures.personWithPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PersonSagaOrchestratorTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonSagaOrchestrator personSagaOrchestrator;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private MedicineClient medicineClient;

    @MockitoBean
    private JobScheduler jobScheduler;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("PERSON_CACHE");
        if (cache != null) {
            cache.clear();
        }
        personRepository.deleteAll();
    }

    @Test
    void createPersonWithPolicySucceeds() {
        var input = personWithPolicy("Dmitry Kuznetsov", 33, "334455", "444444");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return policy(request.getPolicyNumber(), request.getPersonId());
        });

        var created = personSagaOrchestrator.create(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNotNull();
        assertThat(created.getPolicy().getPolicyNumber()).isEqualTo("444444");
        assertThat(created.getPolicy().getPersonId()).isEqualTo(created.getId());
        verify(medicineClient).createPolicyDTO(any(PolicyDTO.class));
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPersonWithPolicyWhenMedicineFailsSavesPersonAndSchedulesJob() {
        var input = personWithPolicy("Nikolay Belov", 29, "667788", "555555");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.serverError("POST", "/policy/created"));

        var created = personSagaOrchestrator.create(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPersonWithPolicyWhenMedicineReturns403SavesPersonAndSchedulesJob() {
        var input = personWithPolicy("Victor Smirnov", 31, "778899", "666666");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.forbidden("POST", "/policy/created"));

        var created = personSagaOrchestrator.create(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPersonWithPolicyWhenTimeoutSavesPersonAndSchedulesJob() {
        var input = personWithPolicy("Egor Lebedev", 26, "889900", "777777");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));

        var created = personSagaOrchestrator.create(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPersonWithoutPolicyDoesNotCallMedicine() {
        var input = personWithoutPolicy("Oleg Kozlov", 40, "778899");

        var created = personSagaOrchestrator.create(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        verify(medicineClient, never()).createPolicyDTO(any());
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }
}
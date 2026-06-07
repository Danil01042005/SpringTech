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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.client.MedicineClient;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.repository.PersonRepository;
import ru.danil.springtech.support.FeignTestExceptions;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;
import static ru.danil.springtech.support.PersonTestFixtures.personWithPolicy;
import static ru.danil.springtech.support.PersonTestFixtures.policyDto;

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
    void getPerson_enrichesPersonWithPolicyFromMedicineService() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Мария Иванова", 30, "654321"));
        UUID personId = saved.getId();
        var policyFromMedicine = policyDto("111111", personId);
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(policyFromMedicine);

        var result = personSagaOrchestrator.getPerson(personId);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getPolicy()).isNotNull();
        assertThat(result.getPolicy().getPolicyNumber()).isEqualTo("111111");
        assertThat(result.getPolicy().getPersonId()).isEqualTo(personId);
        verify(medicineClient).getPolicyByIdDTO(personId);
    }

    @Test
    void getPerson_whenPolicyNotFound_returnsPersonWithoutPolicy() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Сергей Орлов", 27, "556677"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.notFound("GET", "/policy/" + personId));

        var result = personSagaOrchestrator.getPerson(personId);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getPolicy()).isNull();
    }

    @Test
    void getPerson_callsMedicineOnEveryRequestForPolicyEnrichment() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Пётр Сидоров", 28, "112233"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(policyDto("222222", personId));

        personSagaOrchestrator.getPerson(personId);
        personSagaOrchestrator.getPerson(personId);

        verify(medicineClient, times(2)).getPolicyByIdDTO(personId);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getPerson_usesRedisCacheForLocalPersonBetweenCalls() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Ольга Мирная", 32, "443322"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(policyDto("333333", personId));

        personSagaOrchestrator.getPerson(personId);
        clearInvocations(medicineClient);
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(policyDto("333333", personId));

        personSagaOrchestrator.getPerson(personId);

        Cache cache = cacheManager.getCache("PERSON_CACHE");
        assertThat(cache).isNotNull();
        assertThat(cache.get(personId)).isNotNull();
        verify(medicineClient).getPolicyByIdDTO(personId);
    }

    @Test
    void createPerson_withPolicy_enrichesPersonAndCallsMedicine() {
        var input = personWithPolicy("Дмитрий Кузнецов", 33, "334455", "444444");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return policyDto(request.getPolicyNumber(), request.getPersonId());
        });

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNotNull();
        assertThat(created.getPolicy().getPolicyNumber()).isEqualTo("444444");
        assertThat(created.getPolicy().getPersonId()).isEqualTo(created.getId());
        verify(medicineClient).createPolicyDTO(any(PolicyDTO.class));
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_whenMedicineUnavailable_keepsPersonAndSchedulesBackgroundJob() {
        var input = personWithPolicy("Николай Белов", 29, "667788", "555555");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.serverError("POST", "/policy/created"));

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(medicineClient, times(2)).createPolicyDTO(any(PolicyDTO.class));
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_whenMedicineForbidden_compensatesAndThrowsServiceUnavailable() {
        var input = personWithPolicy("Виктор Смирнов", 31, "778899", "666666");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.forbidden("POST", "/policy/created"));

        assertThatThrownBy(() -> personSagaOrchestrator.createPerson(input))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("ошибка данных");

        assertThat(personRepository.findAll()).isEmpty();
        verify(medicineClient, times(1)).createPolicyDTO(any(PolicyDTO.class));
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_whenTimeoutAndPolicyExists_returnsPersonWithPolicy() {
        var input = personWithPolicy("Егор Лебедев", 26, "889900", "777777");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));
        when(medicineClient.getPolicyByIdDTO(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return policyDto("777777", id);
        });

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getPolicy()).isNotNull();
        assertThat(created.getPolicy().getPolicyNumber()).isEqualTo("777777");
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(medicineClient, times(2)).createPolicyDTO(any(PolicyDTO.class));
        verify(medicineClient).getPolicyByIdDTO(eq(created.getId()));
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_whenTimeoutAndPolicyMissing_compensatesAndThrowsServiceUnavailable() {
        var input = personWithPolicy("Игорь Волков", 34, "990011", "888888");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));
        when(medicineClient.getPolicyByIdDTO(any(UUID.class)))
                .thenThrow(FeignTestExceptions.notFound("GET", "/policy/check"));

        assertThatThrownBy(() -> personSagaOrchestrator.createPerson(input))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("не ответил");

        assertThat(personRepository.findAll()).isEmpty();
        verify(jobScheduler, never()).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_whenTimeoutAndMedicineStillDown_keepsPersonAndSchedulesBackgroundJob() {
        var input = personWithPolicy("Роман Орлов", 37, "101010", "999999");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));
        when(medicineClient.getPolicyByIdDTO(any(UUID.class)))
                .thenThrow(FeignTestExceptions.serverError("GET", "/policy/check"));

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void createPerson_withoutPolicy_doesNotCallMedicine() {
        var input = personWithoutPolicy("Олег Козлов", 40, "778899");

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        verify(medicineClient, never()).createPolicyDTO(any());
    }

    @Test
    void getPerson_whenMedicineUnavailable_throwsServiceUnavailableException() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Ирина Соколова", 31, "889900"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.serverError("GET", "/policy/" + personId));

        assertThatThrownBy(() -> personSagaOrchestrator.getPerson(personId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("временно недоступен");
    }

    @Test
    void getPerson_whenMedicineTimesOut_throwsServiceUnavailableException() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Тимур Ахметов", 33, "776655"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.timeout("GET", "/policy/" + personId));

        assertThatThrownBy(() -> personSagaOrchestrator.getPerson(personId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("временно недоступен");
    }
}

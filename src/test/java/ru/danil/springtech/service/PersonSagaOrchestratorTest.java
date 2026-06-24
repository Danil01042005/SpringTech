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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.полис;
import static ru.danil.springtech.support.PersonTestFixtures.человекБезПолиса;
import static ru.danil.springtech.support.PersonTestFixtures.человекСПолисом;

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
    void передКаждымТестом() {
        Cache cache = cacheManager.getCache("PERSON_CACHE");
        if (cache != null) {
            cache.clear();
        }
        personRepository.deleteAll();
    }

    @Test
    void созданиеЧеловека_сПолисом_обогащаетЧеловекаИВызываетМедицину() {
        var input = человекСПолисом("Дмитрий Кузнецов", 33, "334455", "444444");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return полис(request.getPolicyNumber(), request.getPersonId());
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
    void созданиеЧеловека_когдаМедицинаНедоступна_сохраняетЧеловекаИПланируетФоновуюЗадачу() {
        var input = человекСПолисом("Николай Белов", 29, "667788", "555555");
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
    void созданиеЧеловека_когдаМедицинаВозвращает403_компенсируетИБросаетServiceUnavailable() {
        var input = человекСПолисом("Виктор Смирнов", 31, "778899", "666666");
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
    void созданиеЧеловека_приТаймаутеИСуществующемПолисе_возвращаетЧеловекаСПолисом() {
        var input = человекСПолисом("Егор Лебедев", 26, "889900", "777777");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));
        when(medicineClient.getPolicyByIdDTO(any(UUID.class))).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            return полис("777777", id);
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
    void созданиеЧеловека_приТаймаутеИОтсутствииПолиса_планируетФоновуюЗадачу() {
        var input = человекСПолисом("Игорь Волков", 34, "990011", "888888");
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.timeout("POST", "/policy/created"));
        when(medicineClient.getPolicyByIdDTO(any(UUID.class)))
                .thenThrow(FeignTestExceptions.notFound("GET", "/policy/check"));

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        assertThat(personRepository.findById(created.getId())).isPresent();
        verify(jobScheduler).schedule(any(Instant.class), any(JobLambda.class));
    }

    @Test
    void созданиеЧеловека_приТаймаутеИНедоступнойМедицине_сохраняетЧеловекаИПланируетФоновуюЗадачу() {
        var input = человекСПолисом("Роман Орлов", 37, "101010", "999999");
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
    void созданиеЧеловека_безПолиса_неВызываетМедицину() {
        var input = человекБезПолиса("Олег Козлов", 40, "778899");

        var created = personSagaOrchestrator.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        verify(medicineClient, never()).createPolicyDTO(any());
    }
}

package ru.danil.springtech.service;

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
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.repository.PersonRepository;
import ru.danil.springtech.support.FeignTestExceptions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.полис;
import static ru.danil.springtech.support.PersonTestFixtures.человекБезПолиса;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PersonQueryServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonQueryService personQueryService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private MedicineClient medicineClient;

    @BeforeEach
    void передКаждымТестом() {
        Cache cache = cacheManager.getCache("PERSON_CACHE");
        if (cache != null) {
            cache.clear();
        }
        personRepository.deleteAll();
    }

    @Test
    void получениеЧеловека_обогащаетПолисомИзМедицины() {
        var saved = personService.createPersonLocal(человекБезПолиса("Мария Иванова", 30, "654321"));
        UUID personId = saved.getId();
        var policyFromMedicine = полис("111111", personId);
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(policyFromMedicine);

        var result = personQueryService.getPerson(personId);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getPolicy()).isNotNull();
        assertThat(result.getPolicy().getPolicyNumber()).isEqualTo("111111");
        assertThat(result.getPolicy().getPersonId()).isEqualTo(personId);
        verify(medicineClient).getPolicyByIdDTO(personId);
    }

    @Test
    void получениеЧеловека_когдаПолисНеНайден_возвращаетЧеловекаБезПолиса() {
        var saved = personService.createPersonLocal(человекБезПолиса("Сергей Орлов", 27, "556677"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.notFound("GET", "/policy/" + personId));

        var result = personQueryService.getPerson(personId);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getPolicy()).isNull();
    }

    @Test
    void получениеЧеловека_каждыйРазЗапрашиваетПолисВМедицине() {
        var saved = personService.createPersonLocal(человекБезПолиса("Пётр Сидоров", 28, "112233"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(полис("222222", personId));

        personQueryService.getPerson(personId);
        personQueryService.getPerson(personId);

        verify(medicineClient, times(2)).getPolicyByIdDTO(personId);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void получениеЧеловека_второйВызовЧитаетЛокальныеДанныеИзКэша() {
        var saved = personService.createPersonLocal(человекБезПолиса("Ольга Мирная", 32, "443322"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(полис("333333", personId));

        personQueryService.getPerson(personId);
        clearInvocations(medicineClient);
        when(medicineClient.getPolicyByIdDTO(personId)).thenReturn(полис("333333", personId));

        personQueryService.getPerson(personId);

        Cache cache = cacheManager.getCache("PERSON_CACHE");
        assertThat(cache).isNotNull();
        assertThat(cache.get(personId)).isNotNull();
        verify(medicineClient).getPolicyByIdDTO(personId);
    }

    @Test
    void получениеЧеловека_когдаМедицинаНедоступна_бросаетServiceUnavailableException() {
        var saved = personService.createPersonLocal(человекБезПолиса("Ирина Соколова", 31, "889900"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.serviceUnavailable("GET", "/policy/" + personId));

        assertThatThrownBy(() -> personQueryService.getPerson(personId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("временно недоступен");
    }

    @Test
    void получениеЧеловека_когдаТаймаутМедицины_бросаетServiceUnavailableException() {
        var saved = personService.createPersonLocal(человекБезПолиса("Тимур Ахметов", 33, "776655"));
        UUID personId = saved.getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.timeout("GET", "/policy/" + personId));

        assertThatThrownBy(() -> personQueryService.getPerson(personId))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("временно недоступен");
    }
}

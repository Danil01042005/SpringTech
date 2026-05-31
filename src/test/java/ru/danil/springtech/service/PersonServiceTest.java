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
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.client.PolicyClient;
import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exсeption.ObjectNotFoundException;
import ru.danil.springtech.repository.PersonRepository;

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

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private PolicyClient policyClient;

    @BeforeEach
    void clearPersonCache() {
        Cache cache = cacheManager.getCache("PERSON_CACHE");
        if (cache != null) {
            cache.clear();
        }
    }

    @Test
    void createPersonLocal_persistsPersonWithPassport() {
        PersonDTO input = personWithoutPolicy("Иван Петров", 25, "123456");

        PersonDTO saved = personService.createPersonLocal(input);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Иван Петров");
        assertThat(saved.getAge()).isEqualTo(25);
        assertThat(saved.getPassport()).isNotNull();
        assertThat(saved.getPassport().getPassportNumber()).isEqualTo("123456");
        assertThat(personRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void getLocalPerson_whenPersonMissing_throwsObjectNotFoundException() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> personService.getLocalPerson(missingId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void getPerson_enrichesPersonWithPolicyFromMedicineService() {
        PersonDTO saved = personService.createPersonLocal(personWithoutPolicy("Мария Иванова", 30, "654321"));
        UUID personId = saved.getId();
        PolicyDTO policyFromMedicine = policyDto("111111", personId);
        when(policyClient.getPolicyDTO(personId)).thenReturn(policyFromMedicine);

        PersonDTO result = personService.getPerson(personId);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getPolicy()).isNotNull();
        assertThat(result.getPolicy().getPolicyNumber()).isEqualTo("111111");
        assertThat(result.getPolicy().getPersonId()).isEqualTo(personId);
        verify(policyClient).getPolicyDTO(personId);
    }

    @Test
    void getPerson_secondCallReadsFromRedisCacheWithoutCallingMedicineAgain() {
        PersonDTO saved = personService.createPersonLocal(personWithoutPolicy("Пётр Сидоров", 28, "112233"));
        UUID personId = saved.getId();
        when(policyClient.getPolicyDTO(personId)).thenReturn(policyDto("222222", personId));

        personService.getPerson(personId);
        verify(policyClient, times(1)).getPolicyDTO(personId);
        clearInvocations(policyClient);

        personService.getPerson(personId);
        verify(policyClient, never()).getPolicyDTO(eq(personId));

        Cache cache = cacheManager.getCache("PERSON_CACHE");
        assertThat(cache).isNotNull();
        assertThat(cache.get(personId)).isNotNull();
    }

    @Test
    void createPolicy_callsMedicineAndReturnsPolicyWithPersonId() {
        PersonDTO saved = personService.createPersonLocal(
                personWithoutPolicy("Анна Смирнова", 22, "445566"));
        UUID personId = saved.getId();
        PolicyDTO policyRequest = policyDto("333333", null);
        when(policyClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return policyDto(request.getPolicyNumber(), request.getPersonId());
        });

        PolicyDTO createdPolicy = personService.createPolicy(personId, policyRequest);

        assertThat(createdPolicy.getPolicyNumber()).isEqualTo("333333");
        assertThat(createdPolicy.getPersonId()).isEqualTo(personId);
        verify(policyClient).createPolicyDTO(any(PolicyDTO.class));
    }

    @Test
    void createPerson_withoutPolicy_doesNotCallMedicine() {
        PersonDTO input = personWithoutPolicy("Олег Козлов", 40, "778899");

        PersonDTO created = personService.createPerson(input);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getPolicy()).isNull();
        verify(policyClient, times(0)).createPolicyDTO(any());
    }

    @Test
    void deletePersonById_removesPersonFromDatabase() {
        PersonDTO saved = personService.createPersonLocal(personWithoutPolicy("Елена Волкова", 35, "998877"));
        UUID personId = saved.getId();

        personService.deletePersonById(personId);

        assertThat(personRepository.findById(personId)).isEmpty();
    }

    private static PersonDTO personWithoutPolicy(String fullName, int age, String passportNumber) {
        return new PersonDTO(fullName, age, new PassportDTO(passportNumber), null);
    }

    private static PolicyDTO policyDto(String policyNumber, UUID personId) {
        PolicyDTO dto = new PolicyDTO(policyNumber);
        if (personId != null) {
            dto.setPersonId(personId);
        }
        return dto;
    }
}

package ru.danil.springtech.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.client.MedicineClient;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.support.FeignTestExceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.policy;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class MedicineIntegrationServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private MedicineIntegrationService medicineIntegrationService;

    @MockitoBean
    private MedicineClient medicineClient;

    @MockitoBean
    private RetryBudgetService retryBudgetService;

    @Test
    void createPolicyShouldCallMedicineAndReturnPolicyWithPersonId() {
        var saved = personService.createPerson(personWithoutPolicy("Anna Smirnova", 22, "445566"));
        var personId = saved.getId();
        PolicyDTO policyRequest = policy("333333", null);
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return policy(request.getPolicyNumber(), request.getPersonId());
        });

        PolicyDTO createdPolicy = medicineIntegrationService.createPolicyDTO(personId, policyRequest);

        assertThat(createdPolicy.getPolicyNumber()).isEqualTo("333333");
        assertThat(createdPolicy.getPersonId()).isEqualTo(personId);
        verify(medicineClient).createPolicyDTO(any(PolicyDTO.class));
    }

    @Test
    void createPolicyShouldRetryOnServerErrorAccordingToRetryConfig() {
        var saved = personService.createPerson(personWithoutPolicy("Pavel Novikov", 24, "556677"));
        var personId = saved.getId();
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.serverError("POST", "/policy/created"));
        when(retryBudgetService.retry(any(Throwable.class))).thenReturn(true);

        assertThatThrownBy(() ->
                medicineIntegrationService.createPolicyDTO(personId, policy("444444", null))
        ).isInstanceOf(feign.FeignException.class);

        verify(medicineClient, times(2)).createPolicyDTO(any(PolicyDTO.class));
    }

    @Test
    void createPolicyShouldNotRetryOn403() {
        var saved = personService.createPerson(personWithoutPolicy("Svetlana Belova", 27, "667788"));
        var personId = saved.getId();
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class)))
                .thenThrow(FeignTestExceptions.forbidden("POST", "/policy/created"));
        assertThatThrownBy(() ->
                medicineIntegrationService.createPolicyDTO(personId, policy("555555", null))
        ).isInstanceOf(feign.FeignException.Forbidden.class);

        verify(medicineClient, times(1)).createPolicyDTO(any(PolicyDTO.class));
    }

    @Test
    void getPolicyShouldRetryOnServerErrorAccordingToRetryConfig() {
        var personId = personService.createPerson(personWithoutPolicy("Kirill Morozov", 30, "778899")).getId();
        when(medicineClient.getPolicyByIdDTO(personId))
                .thenThrow(FeignTestExceptions.serverError("GET", "/policy/" + personId));
        when(retryBudgetService.retry(any(Throwable.class))).thenReturn(true);

        assertThatThrownBy(() ->
                medicineIntegrationService.getPolicyById(personId)
        ).isInstanceOf(feign.FeignException.class);

        verify(medicineClient, times(2)).getPolicyByIdDTO(personId);
    }
}
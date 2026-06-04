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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;
import static ru.danil.springtech.support.PersonTestFixtures.policyDto;

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

    @Test
    void createPolicyDTO_callsMedicineAndReturnsPolicyWithPersonId() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Анна Смирнова", 22, "445566"));
        var personId = saved.getId();
        PolicyDTO policyRequest = policyDto("333333", null);
        when(medicineClient.createPolicyDTO(any(PolicyDTO.class))).thenAnswer(invocation -> {
            PolicyDTO request = invocation.getArgument(0);
            return policyDto(request.getPolicyNumber(), request.getPersonId());
        });

        PolicyDTO createdPolicy = medicineIntegrationService.createPolicyDTO(personId, policyRequest);

        assertThat(createdPolicy.getPolicyNumber()).isEqualTo("333333");
        assertThat(createdPolicy.getPersonId()).isEqualTo(personId);
        verify(medicineClient).createPolicyDTO(any(PolicyDTO.class));
    }
}

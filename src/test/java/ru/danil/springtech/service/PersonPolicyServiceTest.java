package ru.danil.springtech.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PersonPolicyServiceTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private PersonPolicyService personPolicyService;

    @Test
    void attachPolicyShouldSetPolicyAndStatusAndUpdatePersonInDatabase() {
        UUID personId = UUID.randomUUID();
        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(personId);
        PolicyDTO policyDTO = new PolicyDTO();
        policyDTO.setPolicyNumber("POL-123");

        PersonDTO result = personPolicyService.attachPolicy(personDTO, policyDTO, PolicyStatus.COMPLETED);

        assertThat(result.getPolicy()).isEqualTo(policyDTO);
        assertThat(result.getPolicyStatus()).isEqualTo(PolicyStatus.COMPLETED);
        verify(personService).setPolicyStatusInPerson(personId, PolicyStatus.COMPLETED);
    }

    @Test
    void attachPolicyShouldSetPendingStatusCorrectly() {
        UUID personId = UUID.randomUUID();
        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(personId);
        PolicyDTO policyDTO = null;

        PersonDTO result = personPolicyService.attachPolicy(personDTO, policyDTO, PolicyStatus.PENDING);

        assertThat(result.getPolicy()).isNull();
        assertThat(result.getPolicyStatus()).isEqualTo(PolicyStatus.PENDING);
        verify(personService).setPolicyStatusInPerson(personId, PolicyStatus.PENDING);
    }
}
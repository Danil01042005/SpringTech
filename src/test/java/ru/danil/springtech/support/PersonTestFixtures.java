package ru.danil.springtech.support;

import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.UUID;

public final class PersonTestFixtures {

    private PersonTestFixtures() {
    }

    public static PersonDTO personWithoutPolicy(String fullName, int age, String passportNumber) {
        return new PersonDTO(fullName, age, new PassportDTO(passportNumber), null);
    }

    public static PersonDTO personWithPolicy(String fullName, int age, String passportNumber, String policyNumber) {
        return new PersonDTO(fullName, age, new PassportDTO(passportNumber), new PolicyDTO(policyNumber));
    }

    public static PolicyDTO policyDto(String policyNumber, UUID personId) {
        PolicyDTO dto = new PolicyDTO(policyNumber);
        if (personId != null) {
            dto.setPersonId(personId);
        }
        return dto;
    }
}

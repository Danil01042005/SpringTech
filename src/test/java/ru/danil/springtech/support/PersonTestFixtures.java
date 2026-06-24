package ru.danil.springtech.support;

import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.UUID;

public final class PersonTestFixtures {

    private PersonTestFixtures() {
    }

    public static PersonDTO человекБезПолиса(String fullName, int age, String passportNumber) {
        PersonDTO personDTO = new PersonDTO(fullName, age, new PassportDTO(passportNumber));
        personDTO.setPolicy(null);
        return personDTO;
    }

    public static PersonDTO человекСПолисом(String fullName, int age, String passportNumber, String policyNumber) {
        PersonDTO personDTO = new PersonDTO(fullName, age, new PassportDTO(passportNumber));
        personDTO.setPolicy(new PolicyDTO(policyNumber));
        return personDTO;
    }

    public static PolicyDTO полис(String policyNumber, UUID personId) {
        PolicyDTO dto = new PolicyDTO(policyNumber);
        if (personId != null) {
            dto.setPersonId(personId);
        }
        return dto;
    }
}

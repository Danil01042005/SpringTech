package ru.danil.springtech.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.danil.springtech.util.annotation.MedicineRetry;
import ru.danil.springtech.client.MedicineClient;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineIntegrationService {
    private final MedicineClient medicineClient;

    @MedicineRetry
    public PolicyDTO createPolicyDTO(UUID personId, PolicyDTO policyDTO) {
            policyDTO.setPersonId(personId);
            return medicineClient.createPolicyDTO(policyDTO);
    }

    @MedicineRetry
    public PolicyDTO getPolicyById(UUID personId){
        PolicyDTO policyDTO = medicineClient.getPolicyByIdDTO(personId);
        log.debug("номер полиса: {}", personId);
        return policyDTO;
    }

    public void createPolicyWithoutRetry(UUID personId, PolicyDTO policyDTO) {
        policyDTO.setPersonId(personId);
        medicineClient.createPolicyDTO(policyDTO);
    }
}

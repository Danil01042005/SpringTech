package ru.danil.springtech.client;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class PolicyClient {
    private final MedicineClient medicineClient;

    public PolicyDTO getPolicyDTO(UUID id) throws FeignException.FeignClientException {
        return medicineClient.getPolicyById(id);
    }

    public PolicyDTO createPolicyDTO(PolicyDTO policyDTO) {
        return medicineClient.createPolicy(policyDTO);
    }
}

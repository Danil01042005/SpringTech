package ru.danil.medicine.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.medicine.api.MedicalPolicyApi;
import ru.danil.medicine.dto.PolicyDTO;
import ru.danil.medicine.service.PolicyService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PolicyController implements MedicalPolicyApi {
    private final PolicyService policyService;

    @Override
    public ResponseEntity<PolicyDTO> createPolicy(PolicyDTO policyDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(policyService.create(policyDTO));
    }

    @Override
    public ResponseEntity<PolicyDTO> getPolicyById(UUID id) {
        return ResponseEntity.ok(policyService.get(id));
    }
}

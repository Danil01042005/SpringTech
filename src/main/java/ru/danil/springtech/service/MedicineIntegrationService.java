package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import ru.danil.springtech.client.MedicineClient;
import ru.danil.springtech.config.RetryBudgetConfig;
import ru.danil.springtech.dto.PolicyDTO;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicineIntegrationService {
    private final MedicineClient medicineClient;
    private final RetryBudgetConfig retryBudgetConfig;

    @Retryable(retryFor = FeignException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 100 , multiplier = 2.0, random = true),
            exceptionExpression = "@retryBudgetConfig.retry()"
    )
    public PolicyDTO createPolicyDTO(UUID personId , PolicyDTO policyDTO) {
            policyDTO.setPersonId(personId);
            PolicyDTO newPolicyDTO = medicineClient.createPolicyDTO(policyDTO);
            log.debug("Полис {} создан успешно и привязан к человеку {}" , newPolicyDTO.getPolicyNumber() , newPolicyDTO.getPersonId());
            retryBudgetConfig.successRequest();
            return newPolicyDTO;
    }

    @Retryable(retryFor = FeignException.class, maxAttempts = 2,
            backoff = @Backoff(delay = 100 , multiplier = 2.0, random = true),
            exceptionExpression = "@retryBudgetConfig.retry()"
    )
    public PolicyDTO getPolicyByIdDTO(UUID personId){
        PolicyDTO policyDTO = medicineClient.getPolicyByIdDTO(personId);
        log.debug("Найден полис с айди {}, номер полиса: {}", personId , policyDTO.getPolicyNumber());
        retryBudgetConfig.successRequest();
        return policyDTO;
    }
}

package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.config.RetryBudgetConfig;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.dto.PolicyJobStatus;
import ru.danil.springtech.exception.ServiceUnavailableException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonQueryService {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final RetryBudgetConfig retryBudgetConfig;

    public PersonDTO getPerson(UUID id) {
        PersonDTO personDTO = personService.getLocalPerson(id);
        enrichWithPolicy(personDTO);
        return personDTO;
    }

    private void enrichWithPolicy(PersonDTO personDTO) {
        UUID personId = personDTO.getId();
        try {
            PolicyDTO policyDTO = medicineIntegrationService.getPolicyByIdDTO(personId);
            personDTO.setPolicy(policyDTO);
            personDTO.setPolicyJobStatus(PolicyJobStatus.NUMBER_200);
        } catch (FeignException.NotFound e) {
            log.info("Полис для человека {}  не найден, отдаём данные без полиса", personDTO.toString());
        } catch (FeignException.Forbidden e) {
            personDTO.setPolicyJobStatus(PolicyJobStatus.NUMBER_403);
            log.error("Доступ запрещён при запросе полиса для человека {}: {}", personDTO.toString(), e.getMessage());
        } catch (FeignException e) {
            if (retryBudgetConfig.isQueryRetryable(e)) {
                log.error("Временная недоступность медицины при запросе полиса для человека {}: статус {}", personDTO.toString(), e.status());
                throw new ServiceUnavailableException("Сервис медицины временно недоступен");
            }
            log.error("Бизнес-ошибка при запросе полиса для человека {}: статус {}", personId, e.status());
            throw new RuntimeException("Ошибка интеграции, требующая внимания разработчика");
        }
    }
}

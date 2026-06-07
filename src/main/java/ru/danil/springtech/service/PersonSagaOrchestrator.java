package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exception.ServiceUnavailableException;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonSagaOrchestrator {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;
    private final PolicyBackgroundJobs policyBackgroundJobs;

    public PersonDTO createPerson(PersonDTO newPersonDTO) {
        //Если не был передан полис просто создаем человека локально в сервисе
        if (newPersonDTO.getPolicy() == null) {
            return personService.createPersonLocal(newPersonDTO);
        }

        PersonDTO personDTO = personService.createPersonLocal(newPersonDTO);
        PolicyDTO newPolicyDTO = newPersonDTO.getPolicy();
        try {
            //Пробуем создать Полис
            PolicyDTO policy = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPolicyDTO);
            log.debug("Полис {} создан для человека {}", policy.getPolicyNumber(), policy.getPersonId());
            personDTO.setPolicy(policy);
            return personDTO;
        } catch (FeignException.Forbidden e) {
            //Если валидация не прошла пробуем откатиться
            log.error("Невалидные данные: {}", e.getMessage());
            policyBackgroundJobs.compensateDeleteLocalPerson(personDTO.getId());
            throw new ServiceUnavailableException("Не удалось создать пользователя: ошибка данных");
        } catch (FeignException e) {
            // Если сервис вернул ошибку тайм аута, нам нужно удостовериться создался ли полис
            if (e.status() == -1) {
                return handleCreateTimeout(personDTO, newPolicyDTO);
            }
            log.warn("Медицина не отвечает, полиса гарантирована нет {}", personDTO.getId());
            //Создаем джобу на создание полиса
            policyBackgroundJobs.schedulePolicyCreate(personDTO.getId(), newPolicyDTO);
            return personDTO;
        }
    }

    public PersonDTO getPerson(UUID personId) {
        PersonDTO personDTO = personService.getLocalPerson(personId);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.getPolicyByIdDTO(personId);
            personDTO.setPolicy(policyDTO);
        } catch (FeignException.NotFound e) {
            // Просто возвращаем человека без полиса
            log.info("Полис для человека {} не найден", personId);
            return personDTO;
        } catch (FeignException.ServiceUnavailable | FeignException.InternalServerError e) {
            log.error("Сервис не отвечает {} , {}", personId, e.status());
            throw new ServiceUnavailableException("Сервис медицины временно недоступен");
        } catch (FeignException e) {
            if (e.status() == -1) {
                log.error("Таймаут при получении полиса для человека {}", personId);
                throw new ServiceUnavailableException("Сервис медицины временно недоступен " + personDTO.toString());
            }
            log.error("Неожиданный ответ медицины для человека {}: status {}", personDTO.toString(), e.status());
            throw new ServiceUnavailableException("Сервис медицины временно недоступен");
        }
        return personDTO;
    }

    private PersonDTO handleCreateTimeout(PersonDTO personDTO, PolicyDTO newPolicyDTO) {
        try {
            //Пробуем запросить полис,
            PolicyDTO policy = medicineIntegrationService.getPolicyByIdDTOWithoutRetry(personDTO.getId());
            log.debug("ПРи тайм ауте полис все равно создался: {}", policy.getPolicyNumber());
            personDTO.setPolicy(policy);
            return personDTO;
        } catch (FeignException.NotFound e) {
            log.warn("Полис все таки не был создан {}", personDTO.getId());
            //Раз полис не создался, откатываемся
            policyBackgroundJobs.compensateDeleteLocalPerson(personDTO.getId());
            throw new ServiceUnavailableException("Не удалось создать пользователя: сервис медицины не ответил");
        } catch (FeignException.Forbidden e) {
            log.error("Не удалось получить полис, ошибка ресурса: {}", e.getMessage());
            //Пробуем откатиться
            policyBackgroundJobs.compensateDeleteLocalPerson(personDTO.getId());
            throw new ServiceUnavailableException("Не удалось создать пользователя: ошибка данных");
        } catch (FeignException e) {
            log.warn("Медицина до сих пор не отвечает, {}", personDTO.getId());
            //Создаем джобу на создание полиса
            policyBackgroundJobs.schedulePolicyCreate(personDTO.getId(), newPolicyDTO);
            return personDTO;
        }
    }
}

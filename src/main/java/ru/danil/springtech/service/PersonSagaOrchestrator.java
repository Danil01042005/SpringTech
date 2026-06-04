package ru.danil.springtech.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exсeption.ServiceUnavailableException;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonSagaOrchestrator {
    private final PersonService personService;
    private final MedicineIntegrationService medicineIntegrationService;

    @CachePut(value = "PERSON_CACHE", key = "#result.id")
    public PersonDTO createPerson(PersonDTO newPersonDTO) {
        if(newPersonDTO.getPolicy() != null) {
            PersonDTO personDTO = personService.createPersonLocal(newPersonDTO);
            try {
                PolicyDTO newPolicyDTO = medicineIntegrationService.createPolicyDTO(personDTO.getId(), newPersonDTO.getPolicy());
                log.debug("Полис {} создан успешно и привязан к человеку {}" , newPolicyDTO.getPolicyNumber() , newPolicyDTO.getPersonId());
                personDTO.setPolicy(newPolicyDTO);
                return personDTO;
            } catch (FeignException e) {
                log.error("Не удалось создать пользователя, сервис медецины  не отвечат. Полис: {}", newPersonDTO.getPolicy().toString());
//                Откат создания в локальной бд
                personService.deletePersonById(personDTO.getId());
                throw new ServiceUnavailableException("Не удалось создать пользователя, сервис медецины  не отвечат. Полис: " + newPersonDTO.getPolicy().toString());
            }
        }
        // если человека передали без полиса, просто создаю его локально
        return personService.createPersonLocal(newPersonDTO);
    }

    @Cacheable(value = "PERSON_CACHE", key = "#id")
    public PersonDTO getPerson(UUID id){
        PersonDTO personDTO = personService.getLocalPerson(id);
        try {
            PolicyDTO policyDTO = medicineIntegrationService.getPolicyByIdDTO(id);
            personDTO.setPolicy(policyDTO);
        } catch (FeignException.NotFound e) {
            log.error("Полис по айди {} не найден ", id);
            return personDTO;
        } catch (FeignException e) {
            log.error("Не удалось найти полис,сервис медецины временно не доступен");
            throw new ServiceUnavailableException("Не удалось найти полис,сервис медецины временно не доступен");
        }
        return personDTO;
    }
}

package ru.danil.springtech.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.danil.springtech.controller.controllerAdvice.GlobalExceptionHandlerController;
import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.exсeption.ObjectNotFoundException;
import ru.danil.springtech.exсeption.ServiceUnavailableException;
import ru.danil.springtech.service.PersonSagaOrchestrator;
import ru.danil.springtech.service.PersonService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PersonController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandlerController.class, PersonControllerTest.CacheTestConfig.class})
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private PersonSagaOrchestrator personSagaOrchestrator;

    @Test
    void createPerson_withValidBody_returns201AndPerson() throws Exception {
        UUID personId = UUID.randomUUID();
        PersonDTO response = validPersonResponse(personId);
        when(personSagaOrchestrator.createPerson(any(PersonDTO.class))).thenReturn(response);

        mockMvc.perform(post("/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.passport.passportNumber").value("123456"))
                .andExpect(jsonPath("$.policy.policyNumber").value("654321"));

        verify(personSagaOrchestrator).createPerson(any(PersonDTO.class));
    }

    @Test
    void createPerson_withInvalidAge_returns400() throws Exception {
        mockMvc.perform(post("/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Иван Петров",
                                  "age": 15,
                                  "passport": { "passportNumber": "123456" },
                                  "policy": {
                                    "policyNumber": "654321",
                                    "personId": "11111111-1111-1111-1111-111111111111"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPerson_whenMedicineUnavailable_returns503() throws Exception {
        when(personSagaOrchestrator.createPerson(any(PersonDTO.class)))
                .thenThrow(new ServiceUnavailableException("Не удалось создать пользователя, сервис медецины  не отвечат."));

        mockMvc.perform(post("/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Не удалось создать пользователя, сервис медецины  не отвечат."));
    }

    @Test
    void getPerson_whenExists_returns200AndBody() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId)).thenReturn(validPersonResponse(personId));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.policy.policyNumber").value("654321"));

        verify(personSagaOrchestrator).getPerson(personId);
    }

    @Test
    void getPerson_whenNotFound_returns404AndErrorMessage() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId))
                .thenThrow(new ObjectNotFoundException("Человек с таким айди не найден " + personId));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Человек с таким айди не найден " + personId));
    }

    @Test
    void getPerson_whenMedicineUnavailable_returns503() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId))
                .thenThrow(new ServiceUnavailableException("Не удалось найти полис,сервис медецины временно не доступен"));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Не удалось найти полис,сервис медецины временно не доступен"));
    }

    @Test
    void personUpdate_withValidBody_returns200() throws Exception {
        UUID personId = UUID.randomUUID();
        PersonDTO response = validPersonResponse(personId);
        response.setFullName("Пётр Петров");
        when(personService.updatePerson(eq(personId), any(PersonDTO.class))).thenReturn(response);

        mockMvc.perform(put("/person/update/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Пётр Петров"));

        verify(personService).updatePerson(eq(personId), any(PersonDTO.class));
    }

    @Test
    void deletePerson_whenExists_returns204() throws Exception {
        UUID personId = UUID.randomUUID();

        mockMvc.perform(delete("/person/delete/{id}", personId))
                .andExpect(status().isNoContent());

        verify(personService).deletePersonById(personId);
    }

    private static String validPersonJson() {
        return """
                {
                  "fullName": "Иван Петров",
                  "age": 25,
                  "passport": { "passportNumber": "123456" },
                  "policy": {
                    "policyNumber": "654321",
                    "personId": "11111111-1111-1111-1111-111111111111"
                  }
                }
                """;
    }

    private static PersonDTO validPersonResponse(UUID personId) {
        PersonDTO dto = new PersonDTO(
                "Иван Петров",
                25,
                new PassportDTO("123456"),
                policyDto("654321", personId)
        );
        dto.setId(personId);
        return dto;
    }

    private static PolicyDTO policyDto(String policyNumber, UUID personId) {
        PolicyDTO dto = new PolicyDTO(policyNumber);
        dto.setPersonId(personId);
        return dto;
    }

    @TestConfiguration
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("PERSON_CACHE");
        }
    }
}

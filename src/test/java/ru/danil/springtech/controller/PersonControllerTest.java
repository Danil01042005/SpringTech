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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.danil.springtech.controller.controllerAdvice.GlobalExceptionHandlerController;
import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.exception.ServiceUnavailableException;
import ru.danil.springtech.service.PersonCoordinator;
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
import static ru.danil.springtech.support.PersonTestFixtures.policy;

@WebMvcTest(controllers = PersonController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandlerController.class, PersonControllerTest.configCacheForPerson.class})
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private PersonSagaOrchestrator personSagaOrchestrator;

    @MockitoBean
    private PersonCoordinator personCoordinator;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void createPersonWithValidRequestBodyReturn201AndPerson() throws Exception {
        UUID personId = UUID.randomUUID();
        PersonDTO response = validResponsePerson(personId);
        when(personCoordinator.create(any(PersonDTO.class))).thenReturn(response);

        mockMvc.perform(post("/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPerson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.passport.passportNumber").value("123456"))
                .andExpect(jsonPath("$.policy.policyNumber").value("654321"));

        verify(personCoordinator).create(any(PersonDTO.class));
    }

    @Test
    void createPersonWithInvalidAge_Return400() throws Exception {
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
    void createPersonWhenMedicineUnavailableReturns201WithoutPolicy() throws Exception {
        UUID personId = UUID.randomUUID();
        PersonDTO response = validResponsePerson(personId);
        response.setPolicy(null); // координатор вернёт PENDING без полиса
        when(personCoordinator.create(any(PersonDTO.class))).thenReturn(response);

        mockMvc.perform(post("/person/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPerson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.policy").doesNotExist());

        verify(personCoordinator).create(any(PersonDTO.class));
    }

    @Test
    void getPersonWhenExistsReturns200AndBody() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId)).thenReturn(validResponsePerson(personId));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.policy.policyNumber").value("654321"));

        verify(personSagaOrchestrator).getPerson(personId);
    }

    @Test
    void getPersonWhenNotFoundReturns404AndMessage() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId))
                .thenThrow(new ObjectNotFoundException("Человек с таким айди не найден " + personId));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Человек с таким айди не найден " + personId));
    }

    @Test
    void getPersonWhenMedicineUnavailableReturns503() throws Exception {
        UUID personId = UUID.randomUUID();
        when(personSagaOrchestrator.getPerson(personId))
                .thenThrow(new ServiceUnavailableException("Сервис медицины временно недоступен"));

        mockMvc.perform(get("/person/{id}", personId))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Сервис медицины временно недоступен"));
    }

    @Test
    void updatePersonWithValidBodyReturns200() throws Exception {
        UUID personId = UUID.randomUUID();
        PersonDTO response = validResponsePerson(personId);
        response.setFullName("Пётр Петров");
        when(personService.updatePerson(eq(personId), any(PersonDTO.class))).thenReturn(response);

        mockMvc.perform(put("/person/update/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJsonPerson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.fullName").value("Пётр Петров"));

        verify(personService).updatePerson(eq(personId), any(PersonDTO.class));
    }

    @Test
    void deletePersonWhenExistsReturns204() throws Exception {
        UUID personId = UUID.randomUUID();

        mockMvc.perform(delete("/person/delete/{id}", personId))
                .andExpect(status().isNoContent());

        verify(personService).deletePersonById(personId);
    }

    private static String validJsonPerson() {
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

    private static PersonDTO validResponsePerson(UUID personId) {
        PersonDTO dto = new PersonDTO("Иван Петров", 25, new PassportDTO("123456"));
        dto.setPolicy(policy("654321", personId));
        dto.setId(personId);
        return dto;
    }

    @TestConfiguration
    static class configCacheForPerson {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("PERSON_CACHE");
        }
    }
}
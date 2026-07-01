package ru.danil.springtech.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtech.TestcontainersConfiguration;
import ru.danil.springtech.exception.ObjectNotFoundException;
import ru.danil.springtech.repository.PersonRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.danil.springtech.support.PersonTestFixtures.personWithoutPolicy;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Transactional
class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        Cache cache = cacheManager.getCache("PERSON_CACHE");
        if (cache != null) {
            cache.clear();
        }
        personRepository.deleteAll();
    }

    @Test
    void createPersonSavesPersonWithPassport() {
        var input = personWithoutPolicy("Ivan Petrov", 25, "123456");

        var saved = personService.createPerson(input);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Ivan Petrov");
        assertThat(saved.getAge()).isEqualTo(25);
        assertThat(saved.getPassport()).isNotNull();
        assertThat(saved.getPassport().getPassportNumber()).isEqualTo("123456");
        assertThat(personRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void getPersonWhenNotFoundThrowsObjectNotFoundException() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> personService.getLocalPerson(missingId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void deletePersonRemovesRecordFromDatabase() {
        var saved = personService.createPerson(personWithoutPolicy("Elena Volkova", 35, "998877"));
        UUID personId = saved.getId();

        personService.deletePersonById(personId);

        assertThat(personRepository.findById(personId)).isEmpty();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getPersonSecondCallReadsFromCache() {
        var saved = personService.createPerson(personWithoutPolicy("Natalia Kuznetsova", 29, "556644"));
        UUID personId = saved.getId();

        personService.getLocalPerson(personId);
        personService.getLocalPerson(personId);

        Cache cache = cacheManager.getCache("PERSON_CACHE");
        assertThat(cache).isNotNull();
        assertThat(cache.get(personId)).isNotNull();
    }
}
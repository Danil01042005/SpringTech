package ru.danil.springtech.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
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

    @Test
    void createPersonLocal_persistsPersonWithPassport() {
        var input = personWithoutPolicy("Иван Петров", 25, "123456");

        var saved = personService.createPersonLocal(input);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFullName()).isEqualTo("Иван Петров");
        assertThat(saved.getAge()).isEqualTo(25);
        assertThat(saved.getPassport()).isNotNull();
        assertThat(saved.getPassport().getPassportNumber()).isEqualTo("123456");
        assertThat(personRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void getLocalPerson_whenPersonMissing_throwsObjectNotFoundException() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> personService.getLocalPerson(missingId))
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessageContaining(missingId.toString());
    }

    @Test
    void deletePersonById_removesPersonFromDatabase() {
        var saved = personService.createPersonLocal(personWithoutPolicy("Елена Волкова", 35, "998877"));
        UUID personId = saved.getId();

        personService.deletePersonById(personId);

        assertThat(personRepository.findById(personId)).isEmpty();
    }
}

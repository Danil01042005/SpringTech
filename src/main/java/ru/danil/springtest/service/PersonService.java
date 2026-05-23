package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Person;
import ru.danil.springtest.repository.PersonRepository;
import ru.danil.springtest.utill.ObjectNotFound;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    @Transactional
    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public Person getPerson(UUID id) {
        return personRepository.findById(id).orElseThrow(() -> new ObjectNotFound("Человек с таким айди не найден"));
    }

    @Transactional
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    public Person updatePerson(UUID id, Person updatedPerson) {
        Person person = getPerson(id);
        person.setAge(updatedPerson.getAge());
        person.setFullName(updatedPerson.getFullName());
        if (updatedPerson.getPassport() != null && person.getPassport() != null) {
            person.getPassport().setPassportNumber(updatedPerson.getPassport().getPassportNumber());
        }
        if (updatedPerson.getPassport() != null && person.getPassport() == null) {
            person.setPassport(updatedPerson.getPassport());
        }
        return personRepository.save(person);
    }
}

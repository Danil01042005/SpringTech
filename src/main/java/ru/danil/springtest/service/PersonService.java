package ru.danil.springtest.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.model.Person;
import ru.danil.springtest.repository.PersonRepository;
import ru.danil.springtest.utill.UserExeption;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;

    public Person getPerson(UUID id) {
        return personRepository.findById(id).orElseThrow(() -> new UserExeption("Человек с таким айди не найден", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Person createPerson(Person person){
        person.linkPassport();
        return personRepository.save(person);
    }

    @Transactional
    public void deletePersonById(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    public Person personUpdate(UUID id , Person updatedPerson) {
        Person person = getPerson(id);
        person.setAge(updatedPerson.getAge());
        person.setFullName(updatedPerson.getFullName());
        if (updatedPerson.getPassport() != null) {
            person.getPassport().setPassportNumber(updatedPerson.getPassport().getPassportNumber());
        }
        return personRepository.save(person);
    }
}

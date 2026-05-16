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
        return personRepository.findById(id).orElseThrow(() -> new UserExeption("Челове с таким именем не найден", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Person createNewPerson(Person person){
        person.addPassport();
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
        person.getPassport().setPassportNumber(updatedPerson.getPassport().getPassportNumber());
        person.setFullName(updatedPerson.getFullName());
        return personRepository.save(person);
    }
}

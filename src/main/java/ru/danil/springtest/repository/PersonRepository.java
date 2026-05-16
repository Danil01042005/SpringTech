package ru.danil.springtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danil.springtest.model.Person;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
}

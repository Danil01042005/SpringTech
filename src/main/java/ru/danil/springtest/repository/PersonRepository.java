package ru.danil.springtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.danil.springtest.model.Person;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    @Query("SELECT p FROM Person p LEFT JOIN FETCH p.passport WHERE p.id = :id")
    Optional<Person> findByIdWithPassport(@Param("id") UUID id);
}

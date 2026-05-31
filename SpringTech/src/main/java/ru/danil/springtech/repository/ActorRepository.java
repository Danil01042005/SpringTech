package ru.danil.springtech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.danil.springtech.model.Actor;

import java.util.Optional;
import java.util.UUID;

public interface ActorRepository extends JpaRepository<Actor, UUID> {

    @Query("SELECT a FROM Actor a LEFT JOIN FETCH a.movies WHERE a.id = :id")
    Optional<Actor> findByIdWithMovies(@Param("id") UUID id);
}

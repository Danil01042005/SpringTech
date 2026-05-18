package ru.danil.springtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.danil.springtest.model.Actor;

import java.util.UUID;

public interface ActorRepository extends JpaRepository<Actor, UUID> {
}

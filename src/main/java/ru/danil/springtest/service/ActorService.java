package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.mapper.UserMapper;
import ru.danil.springtest.model.Actor;
import ru.danil.springtest.repository.ActorRepository;
import ru.danil.springtest.utill.ObjectNotFound;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ActorService {

    private final ActorRepository actorRepository;
    private final UserMapper mapper;

    @Transactional(readOnly = true)
    public Actor getActorById(UUID id) {
        return actorRepository.findByIdWithMovies(id).orElseThrow(() -> new ObjectNotFound("Актер с таким айди не найде"));
    }

    @Transactional
    public Actor createActor(Actor actor) {
        return actorRepository.save(actor);
    }

    @Transactional
    public void deleteActorById(UUID id) {
        actorRepository.deleteById(id);
    }

    @Transactional
    public Actor updateActor(UUID id, Actor updatedActor) {
        Actor actor = getActorById(id);
        actor.setName(updatedActor.getName());
        actor.setAge(updatedActor.getAge());
        if (updatedActor.getMovies() != null) {
            actor.getMovies().clear();
            actor.getMovies().addAll(updatedActor.getMovies());
        }
        return actorRepository.save(actor);
    }
}

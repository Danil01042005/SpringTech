package ru.danil.springtest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.danil.springtest.dto.ActorDTO;
import ru.danil.springtest.mapper.ActorMapper;
import ru.danil.springtest.model.Actor;
import ru.danil.springtest.repository.ActorRepository;
import ru.danil.springtest.exeption.ObjectNotFound;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActorService {
    private final ActorRepository actorRepository;
    private final ActorMapper actorMapper;

    @Transactional(readOnly = true)
    public ActorDTO getActorById(UUID id) {
        Actor actor = actorRepository.findByIdWithMovies(id).orElseThrow(() -> {
            log.error("Актер с таким айди не найден: {}", id);
            return new ObjectNotFound("Актер с таким айди не найден: " + id);
        });
        log.debug("Найден актер: id={}, name={}, age={}, createdAt={}, updatedAt={}, isDeleted={}, moviesCount={}",
                actor.getId(), actor.getName(), actor.getAge(), actor.getCreatedAt(),
                actor.getUpdatedAt(), actor.getIsDeleted(), actor.getMovies().size());
        return actorMapper.toActorDTO(actor);
    }

    @Transactional
    public ActorDTO createActor(ActorDTO actorDTO) {
        return actorMapper.toActorDTO(actorRepository.save(actorMapper.toActor(actorDTO)));
    }

    @Transactional
    public void deleteActorById(UUID id) {
        actorRepository.deleteById(id);
    }

    @Transactional
    public ActorDTO updateActor(UUID id, ActorDTO updatedActorDTO) {
        Actor actor = actorMapper.toActor(getActorById(id));
        actorMapper.updateActor(updatedActorDTO, actor);
        return actorMapper.toActorDTO(actor);
    }
}

package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtest.api.ActorsAndFilmsApi;
import ru.danil.springtest.dto.ActorDTO;
import ru.danil.springtest.mapper.ActorMapper;
import ru.danil.springtest.service.ActorService;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class ActorController implements ActorsAndFilmsApi {
    private final ActorService actorService;
    private final ActorMapper actorMapper;

    @Override
    public ResponseEntity<ActorDTO> actorUpdate(UUID id, ActorDTO actorDTO) {
        return ResponseEntity.ok(actorMapper.toActorDTO(actorService.updateActor(id, actorMapper.toActor(actorDTO))));
    }

    @Override
    public ResponseEntity<ActorDTO> createActor(@Valid ActorDTO actorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(actorMapper.toActorDTO(actorService.createActor(actorMapper.toActor(actorDTO))));
    }

    @Override
    public ResponseEntity<Void> deleteActor(UUID id) {
        actorService.deleteActorById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ActorDTO> getActor(UUID id) {
        return ResponseEntity.ok(actorMapper.toActorDTO(actorService.getActorById(id)));
    }
}

package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtest.api.ActorsAndFilmsApi;
import ru.danil.springtest.dto.ActorDTO;
import ru.danil.springtest.service.ActorService;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class ActorController implements ActorsAndFilmsApi {
    private final ActorService actorService;

    @Override
    public ResponseEntity<ActorDTO> actorUpdate(UUID id, ActorDTO updatedActorDTO) {
        return ResponseEntity.ok(actorService.updateActor(id, updatedActorDTO));
    }

    @Override
    public ResponseEntity<ActorDTO> createActor(@Valid ActorDTO actorDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(actorService.createActor(actorDTO));
    }

    @Override
    public ResponseEntity<Void> deleteActor(UUID id) {
        actorService.deleteActorById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ActorDTO> getActor(UUID id) {
        return ResponseEntity.ok(actorService.getActorById(id));
    }
}

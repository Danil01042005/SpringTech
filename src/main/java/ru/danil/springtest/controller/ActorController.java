package ru.danil.springtest.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.danil.springtest.api.ActorsAndFilmsApi;
import ru.danil.springtest.dto.ActorDTO;
import ru.danil.springtest.model.Actor;
import ru.danil.springtest.service.ActorService;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class ActorController implements ActorsAndFilmsApi {

    private final ActorService actorService;
    private final ModelMapper modelMapper;

    @Override
    public ResponseEntity<ActorDTO> actorUpdate(UUID id, ActorDTO actorDTO) {
        return ResponseEntity.ok(convertToActorDTO(actorService.updateActor(id, convertToActor(actorDTO))));
    }

    @Override
    public ResponseEntity<ActorDTO> createActor(@Valid ActorDTO actorDTO) {
        Actor actor = actorService.createActor(convertToActor(actorDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToActorDTO(actor));
    }

    @Override
    public ResponseEntity<Void> deleteActor(UUID id) {
        actorService.deleteActorById(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ActorDTO> getActor(UUID id) {
        return ResponseEntity.ok().body(convertToActorDTO(actorService.getActorById(id)));
    }

    private Actor convertToActor(ActorDTO actorDTO) {
        return modelMapper.map(actorDTO, Actor.class);
    }

    private ActorDTO convertToActorDTO(Actor actor) {
        return modelMapper.map(actor, ActorDTO.class);
    }
}

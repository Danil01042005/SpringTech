package ru.danil.springtech.mapper;

import org.mapstruct.*;
import ru.danil.springtech.dto.ActorDTO;
import ru.danil.springtech.model.Actor;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {
    ActorDTO toActorDTO(Actor actor);
    Actor toActor(ActorDTO actorDTO);
    void updateActor(ActorDTO updateActorDto, @MappingTarget Actor actor);
}

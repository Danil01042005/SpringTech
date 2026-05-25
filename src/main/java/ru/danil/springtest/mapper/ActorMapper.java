package ru.danil.springtest.mapper;

import org.mapstruct.*;
import ru.danil.springtest.dto.ActorDTO;
import ru.danil.springtest.model.Actor;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ActorMapper {
    ActorDTO toActorDTO(Actor actor);
    Actor toActor(ActorDTO actorDTO);
    void updateActor(ActorDTO updateActorDto, @MappingTarget Actor actor);
}

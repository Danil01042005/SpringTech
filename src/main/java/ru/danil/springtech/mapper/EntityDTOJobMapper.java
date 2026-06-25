package ru.danil.springtech.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
@RequiredArgsConstructor
public abstract class EntityDTOJobMapper {
    private final JsonMapper jsonMapper;

    @Mapping(target = "payload", expression = "java(jsonMapper.toJson(personDTO))")
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "entityId", source = "personDTO.id")
    @Mapping(target = "entityName", constant = "PERSON")
    @Mapping(target = "actionName", source = "actionName")
    @Mapping(target = "deferredJobStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    public abstract DeferredJob toPersonJob(PersonDTO personDTO, ActionName actionName);

    @Mapping(target = "payload", expression = "java(jsonMapper.toJson(policyDTO))")
    @Mapping(target = "jobId", ignore = true)
    @Mapping(target = "entityId", source = "personId")
    @Mapping(target = "entityName", constant = "POLICY")
    @Mapping(target = "actionName", source = "actionName")
    @Mapping(target = "deferredJobStatus", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", constant = "false")
    public abstract DeferredJob toPolicyJob(UUID personId, PolicyDTO policyDTO, ActionName actionName);
}

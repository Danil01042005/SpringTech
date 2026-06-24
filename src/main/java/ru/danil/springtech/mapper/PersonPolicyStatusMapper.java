package ru.danil.springtech.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.model.enums.PersonPolicyStatus;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PersonPolicyStatusMapper {
    PolicyStatus toDto(PersonPolicyStatus status);

    PersonPolicyStatus toEntity(PolicyStatus status);
}

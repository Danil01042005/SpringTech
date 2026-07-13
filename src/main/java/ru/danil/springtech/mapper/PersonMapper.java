package ru.danil.springtech.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.model.Person;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    @Mapping(target = "policyStatus", ignore = true)
    Person toPerson(PersonDTO personDTO);

    PersonDTO toPersonDTO(Person person);

    @Mapping(target = "policyStatus", ignore = true)
    void updatePerson(PersonDTO updatedPersonDTO, @MappingTarget Person person);
}

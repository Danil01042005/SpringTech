package ru.danil.springtech.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.model.Person;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
                         unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    Person toPerson(PersonDTO personDTO);
    PersonDTO toPersonDTO(Person person);
    void updatePerson(PersonDTO updatedPersonDTO, @MappingTarget Person person);
}

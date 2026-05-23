package ru.danil.springtest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import ru.danil.springtest.dto.PersonDTO;
import ru.danil.springtest.model.Person;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
                         unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {
    Person toPerson(PersonDTO personDTO);
    PersonDTO toPersonDTO(Person person);
}

package ru.danil.springtest.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtest.dto.PassportDTO;
import ru.danil.springtest.dto.PersonDTO;
import ru.danil.springtest.model.Passport;
import ru.danil.springtest.model.Person;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-23T14:37:09+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PersonMapperImpl implements PersonMapper {

    @Override
    public Person toPerson(PersonDTO personDTO) {
        if ( personDTO == null ) {
            return null;
        }

        Person person = new Person();

        person.setPassport( passportDTOToPassport( personDTO.getPassport() ) );
        person.setFullName( personDTO.getFullName() );
        if ( personDTO.getAge() != null ) {
            person.setAge( personDTO.getAge() );
        }

        return person;
    }

    @Override
    public PersonDTO toPersonDTO(Person person) {
        if ( person == null ) {
            return null;
        }

        PersonDTO personDTO = new PersonDTO();

        personDTO.setFullName( person.getFullName() );
        personDTO.setAge( person.getAge() );
        personDTO.setPassport( passportToPassportDTO( person.getPassport() ) );

        return personDTO;
    }

    protected Passport passportDTOToPassport(PassportDTO passportDTO) {
        if ( passportDTO == null ) {
            return null;
        }

        Passport passport = new Passport();

        passport.setPassportNumber( passportDTO.getPassportNumber() );

        return passport;
    }

    protected PassportDTO passportToPassportDTO(Passport passport) {
        if ( passport == null ) {
            return null;
        }

        PassportDTO passportDTO = new PassportDTO();

        passportDTO.setPassportNumber( passport.getPassportNumber() );

        return passportDTO;
    }
}

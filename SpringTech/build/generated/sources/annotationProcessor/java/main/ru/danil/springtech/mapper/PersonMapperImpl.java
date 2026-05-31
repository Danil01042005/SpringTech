package ru.danil.springtech.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PassportDTO;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.model.Passport;
import ru.danil.springtech.model.Person;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-31T14:10:49+0300",
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
        person.setId( personDTO.getId() );
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

        personDTO.setId( person.getId() );
        personDTO.setFullName( person.getFullName() );
        personDTO.setAge( person.getAge() );
        personDTO.setPassport( passportToPassportDTO( person.getPassport() ) );

        return personDTO;
    }

    @Override
    public void updatePerson(PersonDTO updatedPersonDTO, Person person) {
        if ( updatedPersonDTO == null ) {
            return;
        }

        if ( updatedPersonDTO.getPassport() != null ) {
            if ( person.getPassport() == null ) {
                person.setPassport( new Passport() );
            }
            passportDTOToPassport1( updatedPersonDTO.getPassport(), person.getPassport() );
        }
        else {
            person.setPassport( null );
        }
        person.setId( updatedPersonDTO.getId() );
        person.setFullName( updatedPersonDTO.getFullName() );
        if ( updatedPersonDTO.getAge() != null ) {
            person.setAge( updatedPersonDTO.getAge() );
        }
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

    protected void passportDTOToPassport1(PassportDTO passportDTO, Passport mappingTarget) {
        if ( passportDTO == null ) {
            return;
        }

        mappingTarget.setPassportNumber( passportDTO.getPassportNumber() );
    }
}

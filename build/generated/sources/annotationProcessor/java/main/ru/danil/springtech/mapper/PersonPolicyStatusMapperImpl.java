package ru.danil.springtech.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyStatus;
import ru.danil.springtech.model.enums.PersonPolicyStatus;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-24T17:56:59+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PersonPolicyStatusMapperImpl implements PersonPolicyStatusMapper {

    @Override
    public PolicyStatus toDto(PersonPolicyStatus status) {
        if ( status == null ) {
            return null;
        }

        PolicyStatus policyStatus;

        switch ( status ) {
            case PENDING: policyStatus = PolicyStatus.PENDING;
            break;
            case COMPLETED: policyStatus = PolicyStatus.COMPLETED;
            break;
            case COMPENSATING: policyStatus = PolicyStatus.COMPENSATING;
            break;
            case FAILED: policyStatus = PolicyStatus.FAILED;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + status );
        }

        return policyStatus;
    }

    @Override
    public PersonPolicyStatus toEntity(PolicyStatus status) {
        if ( status == null ) {
            return null;
        }

        PersonPolicyStatus personPolicyStatus;

        switch ( status ) {
            case PENDING: personPolicyStatus = PersonPolicyStatus.PENDING;
            break;
            case COMPLETED: personPolicyStatus = PersonPolicyStatus.COMPLETED;
            break;
            case FAILED: personPolicyStatus = PersonPolicyStatus.FAILED;
            break;
            case COMPENSATING: personPolicyStatus = PersonPolicyStatus.COMPENSATING;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + status );
        }

        return personPolicyStatus;
    }
}

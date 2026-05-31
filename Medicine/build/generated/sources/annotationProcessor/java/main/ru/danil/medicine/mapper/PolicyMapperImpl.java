package ru.danil.medicine.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.medicine.dto.PolicyDTO;
import ru.danil.medicine.model.Policy;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T20:39:30+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PolicyMapperImpl implements PolicyMapper {

    @Override
    public Policy toPolicy(PolicyDTO policyDTO) {
        if ( policyDTO == null ) {
            return null;
        }

        Policy policy = new Policy();

        policy.setPersonId( policyDTO.getPersonId() );
        policy.setPolicyNumber( policyDTO.getPolicyNumber() );

        return policy;
    }

    @Override
    public PolicyDTO toPolicyDTO(Policy policy) {
        if ( policy == null ) {
            return null;
        }

        PolicyDTO policyDTO = new PolicyDTO();

        policyDTO.setPolicyNumber( policy.getPolicyNumber() );
        policyDTO.setPersonId( policy.getPersonId() );

        return policyDTO;
    }
}

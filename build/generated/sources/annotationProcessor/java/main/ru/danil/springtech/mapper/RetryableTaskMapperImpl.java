package ru.danil.springtech.mapper;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.model.RetryableTask;
import ru.danil.springtech.util.converter.PolicyDTOJsonConverter;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-06T15:49:26+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class RetryableTaskMapperImpl implements RetryableTaskMapper {

    @Autowired
    private PolicyDTOJsonConverter policyDTOJsonConverter;

    @Override
    public RetryableTask toRetryableTask(PolicyDTO policyDTO, RetryableTaskType type) {
        if ( policyDTO == null && type == null ) {
            return null;
        }

        RetryableTask retryableTask = new RetryableTask();

        retryableTask.setPayload( policyDTOJsonConverter.toJson( policyDTO ) );
        retryableTask.setType( type );

        return retryableTask;
    }

    @Override
    public RetryableTaskDTO toRetryableTaskDTO(RetryableTask retryableTask) {
        if ( retryableTask == null ) {
            return null;
        }

        RetryableTaskDTO retryableTaskDTO = new RetryableTaskDTO();

        retryableTaskDTO.setId( retryableTask.getId() );
        retryableTaskDTO.setType( retryableTask.getType() );
        retryableTaskDTO.setPayload( retryableTask.getPayload() );
        retryableTaskDTO.setRetryTime( map( retryableTask.getRetryTime() ) );
        retryableTaskDTO.setAttempts( retryableTask.getAttempts() );

        return retryableTaskDTO;
    }

    @Override
    public PolicyDTO toPolicyDTOFromPayloadOfRetryableTask(RetryableTaskDTO retryableTaskDTO) {
        if ( retryableTaskDTO == null ) {
            return null;
        }

        PolicyDTO policyDTO = new PolicyDTO();

        return policyDTO;
    }
}

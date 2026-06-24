package ru.danil.springtech.mapper;

import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.DeferredJobStatus;
import ru.danil.springtech.model.enums.EntityName;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-24T17:56:59+0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-language-java-9.4.1.jar, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class EntityDTOJobMapperImpl extends EntityDTOJobMapper {

    @Override
    public DeferredJob toPersonJob(PersonDTO personDTO, ActionName actionName) {
        if ( personDTO == null && actionName == null ) {
            return null;
        }

        DeferredJob deferredJob = new DeferredJob();

        if ( personDTO != null ) {
            deferredJob.setEntityId( personDTO.getId() );
        }
        deferredJob.setActionName( actionName );
        deferredJob.setPayload( jsonMapper.toJson(personDTO) );
        deferredJob.setEntityName( EntityName.PERSON );
        deferredJob.setDeferredJobStatus( DeferredJobStatus.PENDING );
        deferredJob.setIsDeleted( false );

        return deferredJob;
    }

    @Override
    public DeferredJob toPolicyJob(UUID personId, PolicyDTO policyDTO, ActionName actionName) {
        if ( personId == null && policyDTO == null && actionName == null ) {
            return null;
        }

        DeferredJob deferredJob = new DeferredJob();

        deferredJob.setEntityId( personId );
        deferredJob.setActionName( actionName );
        deferredJob.setPayload( jsonMapper.toJson(policyDTO) );
        deferredJob.setEntityName( EntityName.POLICY );
        deferredJob.setDeferredJobStatus( DeferredJobStatus.PENDING );
        deferredJob.setIsDeleted( false );

        return deferredJob;
    }
}

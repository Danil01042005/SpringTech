package ru.danil.springtech.util.job.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.mapper.JsonMapper;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.EntityName;
import ru.danil.springtech.util.JobHandler;
import ru.danil.springtech.util.job.PolicyBackgroundJob;

@Component
@RequiredArgsConstructor
public class PolicyJobHandler implements JobHandler {
    private final PolicyBackgroundJob policyBackgroundJob;
    private final JsonMapper jsonMapper;

    @Override
    public void handle(DeferredJob deferredJob) {
        PolicyDTO policyDTO = jsonMapper.fromJson(deferredJob.getPayload(), PolicyDTO.class);
        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(deferredJob.getEntityId());
        switch (deferredJob.getActionName()) {
            case ActionName.CREATE -> policyBackgroundJob.scheduleCreatePolicyWithBudget(personDTO, policyDTO);
        }
    }

    @Override
    public EntityName getSupportedEntity() {
        return EntityName.POLICY;
    }

    @Override
    public Class getDtoClass() {
        return PolicyDTO.class;
    }
}

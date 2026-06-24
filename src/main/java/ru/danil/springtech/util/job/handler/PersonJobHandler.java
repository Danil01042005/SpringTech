package ru.danil.springtech.util.job.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PersonDTO;
import ru.danil.springtech.mapper.JsonMapper;
import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.EntityName;
import ru.danil.springtech.util.JobHandler;
import ru.danil.springtech.util.job.PersonBackgroundJob;

@Component
@RequiredArgsConstructor
public class PersonJobHandler implements JobHandler {
    private final PersonBackgroundJob personBackgroundJob;
    private final JsonMapper jsonMapper;

    @Override
    public void handle(DeferredJob deferredJob) {
        PersonDTO personDTO = jsonMapper.fromJson(deferredJob.getPayload(), PersonDTO.class);
        switch (deferredJob.getActionName()) {
            case ActionName.DELETE -> personBackgroundJob.deleteLocalPersonJob(personDTO);
        }
    }

    @Override
    public EntityName getSupportedEntity() {
        return EntityName.PERSON;
    }

    @Override
    public Class getDtoClass() {
        return PersonDTO.class;
    }
}

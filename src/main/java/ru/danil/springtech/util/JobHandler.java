package ru.danil.springtech.util;

import ru.danil.springtech.model.DeferredJob;
import ru.danil.springtech.model.enums.ActionName;
import ru.danil.springtech.model.enums.EntityName;

public interface JobHandler<T> {
    void handle(DeferredJob deferredJob);
    EntityName getSupportedEntity();
    Class<T> getDtoClass();
}

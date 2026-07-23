package ru.danil.springtech.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.danil.springtech.dto.PolicyStatus;

@Converter
public class PolicyStatusConverter implements AttributeConverter<PolicyStatus, String> {
    @Override
    public String convertToDatabaseColumn(PolicyStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PolicyStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : PolicyStatus.fromValue(dbData);
    }
}

package ru.danil.springtech.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.danil.springtech.kafka.dto.RetryableTaskType;

@Converter
public class RetryableTaskTypeConverter implements AttributeConverter<RetryableTaskType, String> {
    @Override
    public String convertToDatabaseColumn(RetryableTaskType attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RetryableTaskType convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : RetryableTaskType.fromValue(dbData);
    }
}

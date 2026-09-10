package ru.danil.springtech.util.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.danil.springtech.kafka.dto.RetryableTaskStatus;

@Converter
public class RetryableTaskStatusConverter implements AttributeConverter<RetryableTaskStatus, String> {
    @Override
    public String convertToDatabaseColumn(RetryableTaskStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RetryableTaskStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : RetryableTaskStatus.fromValue(dbData);
    }
}

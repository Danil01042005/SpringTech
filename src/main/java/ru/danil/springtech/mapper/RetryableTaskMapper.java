package ru.danil.springtech.mapper;

import org.mapstruct.*;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.model.RetryableTask;
import ru.danil.springtech.util.converter.PolicyDTOJsonConverter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PolicyDTOJsonConverter.class)
public interface RetryableTaskMapper {
    @Mapping(source = "policyDTO", target = "payload" , qualifiedByName = "convertObjectToJson")
    RetryableTask toRetryableTask(PolicyDTO policyDTO, RetryableTaskType type);

    RetryableTaskDTO toRetryableTaskDTO(RetryableTask retryableTask);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Mapping(source = "payload", target = ".", qualifiedByName = "convertJsonToPolicyDTO")
    PolicyDTO toPolicyDTOFromPayloadOfRetryableTask(RetryableTaskDTO retryableTaskDTO);
}

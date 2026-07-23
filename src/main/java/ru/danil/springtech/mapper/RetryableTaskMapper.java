package ru.danil.springtech.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.*;
import ru.danil.springtech.dto.PolicyDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskDTO;
import ru.danil.springtech.kafka.dto.RetryableTaskType;
import ru.danil.springtech.model.RetryableTask;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RetryableTaskMapper {

    @Mapping(source = "policyDTO", target = "payload" , qualifiedByName = "convertObjectToJson")
    RetryableTask toRetryableTask(PolicyDTO policyDTO, RetryableTaskType type);

    @Named("convertObjectToJson")
    default String convertObjectToJson(PolicyDTO policyDTO){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(policyDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при конвертации полиса в json",e);
        }
    }

    RetryableTaskDTO toRetryableTaskDTO(RetryableTask retryableTask);

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Mapping(source = "retryableTaskDTO",target = ".", qualifiedByName = "convertJsonToPolicyDTO")
    PolicyDTO toPolicyDTOFromPayloadOfRetryableTask(RetryableTaskDTO retryableTaskDTO);

    @Named("convertJsonToPolicyDTO")
    default PolicyDTO convertJsonToPolicyDTO(String json){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, PolicyDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка парсинга PolicyDTO из JSON", e);
        }
    }
}

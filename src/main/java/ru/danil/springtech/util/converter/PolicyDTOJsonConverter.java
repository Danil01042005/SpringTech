package ru.danil.springtech.util.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ru.danil.springtech.dto.PolicyDTO;

@Component
@RequiredArgsConstructor
public class PolicyDTOJsonConverter {
    private final ObjectMapper objectMapper;

    @Named("convertObjectToJson")
    public String toJson(PolicyDTO policyDTO) {
        try {
            return objectMapper.writeValueAsString(policyDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при конвертации PolicyDTO", e);
        }
    }

    @Named("convertJsonToPolicyDTO")
    public PolicyDTO fromJson(String json) {
        try {
            return objectMapper.readValue(json, PolicyDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка парсинга PolicyDTO", e);
        }
    }
}

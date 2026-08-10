package ru.danil.springtech.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "kafka-topic")
public class KafkaConfigProperties {
    @NotNull(message = "Количество партиций не должно быть null")
    @Min(value = 1, message = "Количество партиций должно быть >= 1")
    private Integer partitions;

    @NotNull(message = "Количество реплик не должно быть null")
    @Min(value = 1, message = "Количество реплик должно быть >= 1")
    private Integer replicas;

    @NotBlank(message = "minInsyncReplicas не должен быть пустым")
    private String minInsyncReplicas;

    @NotBlank(message = "Имя топика не должно быть пустым")
    private String policyCreatedTopicName;
}

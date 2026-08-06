package ru.danil.springtech.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "retryable-task-service")
@Component
public class RetryableTaskProperties {
    @NotNull
    @Min(1)
    private Integer limit;

    @NotNull
    @Min(1)
    private int retryDelaySeconds;

    @NotNull
    @Min(1)
    private int processingLeaseSeconds;

    @NotNull
    @Min(1)
    private Integer maxAttempts;
}

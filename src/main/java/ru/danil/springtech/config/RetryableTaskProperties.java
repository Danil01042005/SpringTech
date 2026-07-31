package ru.danil.springtech.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "retryable-task-service")
@Component
public class RetryableTaskProperties {
    private Integer limit;
    private Integer timeoutInSecond;
}

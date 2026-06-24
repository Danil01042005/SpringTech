package ru.danil.springtech.config;

import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "retry-config")
public class RetryBudgetConfig {
    private final StringRedisTemplate redisTemplate;
    private final Map<String, RedisScript<Long>> redisScripts;

    private String tokenBudgetKeyName;
    private int maxAttempts;
    private long delay;
    private double multiplier;
    private int maxCapacity;
    private int successRequest;
    private int retryCost;
    private List<Integer> commandRetryableStatuses;
    private List<Integer> commandNotRetryableStatuses;
    private List<Integer> queryRetryableStatuses;
    private List<Integer> queryNotRetryableStatuses;

    @PostConstruct
    public void initKey() {
        redisTemplate.opsForValue().setIfAbsent(tokenBudgetKeyName, String.valueOf(maxCapacity));
    }

    public boolean isCommandRetryable(Throwable throwable) {
        return checkRetryStatus(throwable, commandRetryableStatuses, commandNotRetryableStatuses);
    }

    public boolean isCommandNotRetryable(FeignException error) {
        return commandNotRetryableStatuses != null && commandNotRetryableStatuses.contains(error.status());
    }

    public boolean isQueryRetryable(Throwable throwable) {
        return checkRetryStatus(throwable, queryRetryableStatuses, queryNotRetryableStatuses);
    }

    public boolean isQueryNotRetryable(FeignException error) {
        return queryNotRetryableStatuses != null && queryNotRetryableStatuses.contains(error.status());
    }

    public boolean retry(Throwable throwable) {
        if (!isCommandRetryable(throwable)) {
            return false;
        }
        return retryScriptExecute();
    }

    public void successRequest() {
        RedisScript<Long> script = redisScripts.get("successRequest");
        redisTemplate.execute(
                script,
                Collections.singletonList(tokenBudgetKeyName),
                String.valueOf(successRequest),
                String.valueOf(maxCapacity)
        );
    }

    public boolean retryScriptExecute() {
        RedisScript<Long> script = redisScripts.get("retry");
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(tokenBudgetKeyName),
                String.valueOf(retryCost)
        );
        return result != null && result >= 0;
    }

    public boolean retryQuery(Throwable throwable) {
        if (!isQueryRetryable(throwable)) {
            return false;
        }
        return retryScriptExecute();
    }

    public boolean shouldCompensate(FeignException error, boolean afterTimeoutVerify) {
        if (!isCommandNotRetryable(error)) {
            return false;
        }
        if (error.status() == 404) {
            return !afterTimeoutVerify;
        }
        return true;
    }

    public boolean shouldRetryLater(FeignException error) {
        if (error.status() == -1) {
            return true;
        }
        if (isCommandRetryable(error)) {
            return true;
        }
        return error.status() == 404;
    }

    private boolean checkRetryStatus(Throwable throwable, List<Integer> retryable, List<Integer> notRetryable) {
        if (!(throwable instanceof FeignException e)) {
            return false;
        }
        int status = e.status();
        if (notRetryable != null && notRetryable.contains(status)) {
            return false;
        }
        if (status == -1) {
            return true;
        }
        return retryable != null && retryable.contains(status);
    }
}

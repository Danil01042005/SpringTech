package ru.danil.springtech.service;

import feign.FeignException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@RequiredArgsConstructor
@Service
@ConfigurationProperties(prefix = "retry-config")
public class RetryBudgetService {
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

    @PostConstruct
    public void initKey() {
        redisTemplate.opsForValue().setIfAbsent(tokenBudgetKeyName, String.valueOf(maxCapacity));
    }

    public boolean isCommandRetryable(Throwable throwable) {
        return checkRetryStatus(throwable, commandRetryableStatuses, commandNotRetryableStatuses);
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

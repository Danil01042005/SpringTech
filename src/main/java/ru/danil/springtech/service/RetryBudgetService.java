package ru.danil.springtech.service;

import feign.FeignException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Service
@ConfigurationProperties(prefix = "retry-config")
public class RetryBudgetService {

    private final StringRedisTemplate redisTemplate;

    private String tokenBudgetKeyName;
    private int maxAttempts;
    private long delay;
    private double multiplier;
    private int maxCapacity;
    private int successRequest;
    private int retryCost;
    private long ttlSeconds = 60;

    private List<Integer> commandRetryableStatuses;
    private List<Integer> commandNotRetryableStatuses;

    public boolean isCommandRetryable(Throwable throwable) {
        return checkRetryStatus(throwable, commandRetryableStatuses, commandNotRetryableStatuses);
    }

    public boolean retry(Throwable throwable) {
        if (!isCommandRetryable(throwable)) {
            return false;
        }
        return retryBudget();
    }
    public void successRequest() {
        Long current = incrementAndGet(tokenBudgetKeyName, successRequest);
        if (current != null && current > maxCapacity) {
            setValueWithTtl(tokenBudgetKeyName, maxCapacity);
        } else {
            refreshTtl(tokenBudgetKeyName);
        }
    }

    public boolean retryBudget() {
        String val = redisTemplate.opsForValue().get(tokenBudgetKeyName);
        long current = val == null ? 0 : Long.parseLong(val);
        if (current < retryCost) {
            return false;
        }
        Long afterDecr = decrementAndGet(tokenBudgetKeyName, retryCost);
        if (afterDecr != null) {
            refreshTtl(tokenBudgetKeyName);
        }
        return afterDecr != null && afterDecr >= 0;
    }

    private Long incrementAndGet(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    private Long decrementAndGet(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    private void setValueWithTtl(String key, long value) {
        redisTemplate.opsForValue().set(key, String.valueOf(value), Duration.ofSeconds(ttlSeconds));
    }

    private void refreshTtl(String key) {
        redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
    }

    private boolean checkRetryStatus(Throwable throwable, List<Integer> retryable, List<Integer> notRetryable
    ) {
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
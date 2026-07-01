package ru.danil.springtech.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryBudgetServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisScript<Long> retryScript;

    @Mock
    private RedisScript<Long> successScript;

    private RetryBudgetService service;

    @BeforeEach
    void setUp() {
        Map<String, RedisScript<Long>> scripts = new HashMap<>();
        scripts.put("retry", retryScript);
        scripts.put("successRequest", successScript);

        service = new RetryBudgetService(redisTemplate, scripts);

        service.setTokenBudgetKeyName("retry-budget");
        service.setMaxCapacity(100);
        service.setSuccessRequest(1);
        service.setRetryCost(1);
        service.setCommandRetryableStatuses(List.of(429, 500, 502, 503, 504));
        service.setCommandNotRetryableStatuses(List.of(400, 401, 403, 404));

        // заглушка для redisTemplate.opsForValue() убрана отсюда
    }

    @Test
    void initKeyShouldSetIfAbsentWithMaxCapacity() {
        // эта заглушка нужна только этому тесту
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.initKey();
        verify(valueOperations).setIfAbsent("retry-budget", "100");
    }

    @Test
    void retryScriptExecuteShouldReturnTrueWhenResultPositive() {
        when(redisTemplate.execute(eq(retryScript), anyList(), eq("1"))).thenReturn(1L);
        assertThat(service.retryScriptExecute()).isTrue();
    }

    @Test
    void retryScriptExecuteShouldReturnFalseWhenResultNegative() {
        when(redisTemplate.execute(eq(retryScript), anyList(), eq("1"))).thenReturn(-1L);
        assertThat(service.retryScriptExecute()).isFalse();
    }

    @Test
    void retryScriptExecuteShouldReturnFalseWhenResultNull() {
        when(redisTemplate.execute(eq(retryScript), anyList(), eq("1"))).thenReturn(null);
        assertThat(service.retryScriptExecute()).isFalse();
    }

    @Test
    void successRequestShouldExecuteScriptWithCorrectParameters() {
        service.successRequest();
        verify(redisTemplate).execute(eq(successScript), anyList(), eq("1"), eq("100"));
    }

    @Test
    void isCommandRetryableShouldReturnTrueForRetryableStatus() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(503);
        assertThat(service.isCommandRetryable(ex)).isTrue();
    }

    @Test
    void isCommandRetryableShouldReturnFalseForNotRetryableStatus() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(403);
        assertThat(service.isCommandRetryable(ex)).isFalse();
    }

    @Test
    void isCommandRetryableShouldReturnTrueForTimeout() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(-1);
        assertThat(service.isCommandRetryable(ex)).isTrue();
    }

    @Test
    void isCommandRetryableShouldReturnFalseForNonFeignException() {
        assertThat(service.isCommandRetryable(new RuntimeException())).isFalse();
    }

    @Test
    void retryShouldReturnFalseWhenNotRetryable() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(400);
        assertThat(service.retry(ex)).isFalse();
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), anyString());
    }

    @Test
    void retryShouldReturnTrueWhenRetryableAndBudgetAvailable() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(503);
        when(redisTemplate.execute(eq(retryScript), anyList(), eq("1"))).thenReturn(1L);
        assertThat(service.retry(ex)).isTrue();
    }
}
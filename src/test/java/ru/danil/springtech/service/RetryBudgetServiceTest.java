package ru.danil.springtech.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryBudgetServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RetryBudgetService service;

    private static final String KEY = "retry-budget";
    private static final long TTL = 60;

    @BeforeEach
    void setUp() {
        service = new RetryBudgetService(redisTemplate);
        service.setTokenBudgetKeyName(KEY);
        service.setMaxCapacity(10);
        service.setSuccessRequest(2);
        service.setRetryCost(3);
        service.setTtlSeconds(TTL);
        service.setCommandRetryableStatuses(List.of(429, 500, 502, 503, 504));
        service.setCommandNotRetryableStatuses(List.of(400, 401, 403, 404));
    }

    private void givenValueOperations() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void successRequestShouldIncrementAndSetTtlWhenBelowMaxCapacity() {
        givenValueOperations();
        when(valueOperations.increment(KEY, 2L)).thenReturn(8L);
        service.successRequest();
        verify(valueOperations).increment(KEY, 2L);
        verify(redisTemplate).expire(KEY, Duration.ofSeconds(TTL));
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void successRequestShouldCapAtMaxCapacity() {
        givenValueOperations();
        when(valueOperations.increment(KEY, 2L)).thenReturn(11L);
        service.successRequest();
        verify(valueOperations).increment(KEY, 2L);
        verify(valueOperations).set(KEY, "10", Duration.ofSeconds(TTL));
        verify(redisTemplate, never()).expire(eq(KEY), any());
    }

    @Test
    void successRequestShouldHandleNullIncrement() {
        givenValueOperations();
        when(valueOperations.increment(KEY, 2L)).thenReturn(null);
        service.successRequest();
        verify(valueOperations).increment(KEY, 2L);
        verify(redisTemplate).expire(KEY, Duration.ofSeconds(TTL));
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void retryBudgetShouldReturnTrueWhenEnoughBudget() {
        givenValueOperations();
        when(valueOperations.get(KEY)).thenReturn("5");
        when(valueOperations.decrement(KEY, 3L)).thenReturn(2L);
        boolean result = service.retryBudget();
        assertThat(result).isTrue();
        verify(valueOperations).get(KEY);
        verify(valueOperations).decrement(KEY, 3L);
        verify(redisTemplate).expire(KEY, Duration.ofSeconds(TTL));
    }

    @Test
    void retryBudgetShouldReturnFalseWhenNotEnoughBudget() {
        givenValueOperations();
        when(valueOperations.get(KEY)).thenReturn("2");
        boolean result = service.retryBudget();
        assertThat(result).isFalse();
        verify(valueOperations).get(KEY);
        verify(valueOperations, never()).decrement(anyString(), anyLong());
    }

    @Test
    void retryBudgetShouldReturnFalseWhenKeyMissing() {
        givenValueOperations();
        when(valueOperations.get(KEY)).thenReturn(null);
        boolean result = service.retryBudget();
        assertThat(result).isFalse();
        verify(valueOperations, never()).decrement(anyString(), anyLong());
    }

    @Test
    void retryBudgetShouldReturnFalseWhenDecrementReturnsNull() {
        givenValueOperations();
        when(valueOperations.get(KEY)).thenReturn("5");
        when(valueOperations.decrement(KEY, 3L)).thenReturn(null);
        boolean result = service.retryBudget();
        assertThat(result).isFalse();
        verify(redisTemplate, never()).expire(eq(KEY), any());
    }

    @Test
    void retryBudgetShouldReturnFalseWhenDecrementGoesBelowZero() {
        givenValueOperations();
        when(valueOperations.get(KEY)).thenReturn("5");
        when(valueOperations.decrement(KEY, 3L)).thenReturn(-1L);
        boolean result = service.retryBudget();
        assertThat(result).isFalse();
        verify(redisTemplate).expire(KEY, Duration.ofSeconds(TTL));
    }

    @Test
    void retryShouldReturnFalseWhenNotRetryable() {
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(400);
        assertThat(service.retry(ex)).isFalse();
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    void retryShouldDelegateToRetryBudgetWhenRetryable() {
        givenValueOperations();
        FeignException ex = mock(FeignException.class);
        when(ex.status()).thenReturn(503);
        when(valueOperations.get(KEY)).thenReturn("5");
        when(valueOperations.decrement(KEY, 3L)).thenReturn(2L);
        assertThat(service.retry(ex)).isTrue();
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
}
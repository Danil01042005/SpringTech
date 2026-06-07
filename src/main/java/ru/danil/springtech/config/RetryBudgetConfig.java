package ru.danil.springtech.config;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RetryBudgetConfig {

    @Value("${retry-config.retry-budget.max-capacity:100}")
    private int maxCapacity;

    @Value("${retry-config.retry-budget.success-request:1}")
    private int successRequest;

    @Value("${retry-config.retry-budget.retry-cost:10}")
    private int retryCost;

    private final AtomicInteger tokenCounter = new AtomicInteger(100);

    public void successRequest() {
        tokenCounter.updateAndGet(tokens -> Math.min(maxCapacity, tokens + successRequest));
    }
    //Проверя подходит ли статус ошибки для ретрая , чтобы игнорировать not found и прочую нечесть
    private boolean isRetryable(Throwable throwable) {
        if (!(throwable instanceof FeignException e)) {
            return false;
        }

        int status = e.status();
        return switch (status) {
            case -1, 429, 500,502,503,504 -> true;
            default -> false;
        };
    }

    // В анотацие которую я создал есть xceptionExpression = "@retryBudgetConfig.retry(#root)"
    // - root это что то типо плейсхолдера который будет перехватывать все исключения и вызывать этот метод с проверкой статуса
    public boolean retry(Throwable throwable) {
        if(isRetryable(throwable)) {
            return retry();
        }
        return false;
    }

    public boolean retry() {
        while (true) {
            int tokens = tokenCounter.get();
            if (tokens < retryCost) return false;
            if (tokenCounter.compareAndSet(tokens, tokens - retryCost)) {
                return true;
            }
        }
    }
}
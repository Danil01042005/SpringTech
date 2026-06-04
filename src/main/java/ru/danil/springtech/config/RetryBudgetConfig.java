package ru.danil.springtech.config;

import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RetryBudgetConfig {
    private final int MAX_CAPACITY = 100;
    private final int SUCCESS_REQUEST = 1;
    private final int RETRY = 10;
    private final AtomicInteger tokenCounter = new AtomicInteger(100);

    public void successRequest() {
        tokenCounter.updateAndGet(tokens -> Math.min(MAX_CAPACITY, tokens + SUCCESS_REQUEST));
    }

    public boolean retry() {
        while (true) {
            int tokens = tokenCounter.get();
            if (tokens < 10) return false;
            if (tokens >= 10) {
                tokenCounter.compareAndSet(tokens, tokens - RETRY);
                return true;
            }
        }
    }
}

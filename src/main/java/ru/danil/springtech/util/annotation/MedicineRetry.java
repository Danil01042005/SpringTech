package ru.danil.springtech.util.annotation;

import feign.FeignException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(retryFor = FeignException.class, maxAttemptsExpression = "${retry-config.max-attempts}",
        backoff = @Backoff(delayExpression = "${retry-config.delay}" , multiplierExpression = "${retry-config.multiplier}", random = true),
        exceptionExpression = "@retryBudgetService.retry(#root)"
)
public @interface MedicineRetry {
}

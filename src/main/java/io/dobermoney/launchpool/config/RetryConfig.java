package io.dobermoney.launchpool.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClientResponseException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Configuration
public class RetryConfig {
    private static final int MAX_ATTEMPTS = 5;
    private static final long DEFAULT_RETRY_AFTER_SECONDS = 60;

    @Bean
    public RetryTemplate coingeckoRetryTemplate() {
        var retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier(throwable -> {
            if (isRateLimitError(throwable)) {
                return new SimpleRetryPolicy(MAX_ATTEMPTS);
            }
            return new NeverRetryPolicy();
        });

        var retryTemplate = RetryTemplate.builder()
                .customPolicy(retryPolicy)
                .customBackoff(new RetryAfterBackOffPolicy())
                .build();

        retryTemplate.registerListener(new RetryListener() {
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                long retryAfterSeconds = parseRetryAfter(throwable);
                RetryAfterBackOffPolicy.setRetryAfterSeconds(context, retryAfterSeconds);
                log.warn("Coingecko rate limit (429), Retry-After: {}s, retry {}/{}",
                        retryAfterSeconds, context.getRetryCount(), MAX_ATTEMPTS);
            }
        });

        return retryTemplate;
    }

    private static boolean isRateLimitError(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof RestClientResponseException rcre) {
                return rcre.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Parses Retry-After header from the exception. Supports:
     * - Delay in seconds (e.g. "60")
     * - HTTP-date (e.g. "Wed, 21 Oct 2015 07:28:00 GMT")
     */
    private static long parseRetryAfter(Throwable e) {
        RestClientResponseException rcre = findRestClientResponseException(e);
        if (rcre == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        HttpHeaders headers = rcre.getResponseHeaders();
        if (headers == null) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        String value = headers.getFirst("Retry-After");
        if (value == null || value.isBlank()) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            // Try parsing as HTTP-date
        }
        try {
            ZonedDateTime retryDate = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME);
            long seconds = ChronoUnit.SECONDS.between(ZonedDateTime.now(), retryDate);
            return Math.max(1, seconds);
        } catch (Exception ignored) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }

    private static RestClientResponseException findRestClientResponseException(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof RestClientResponseException rcre) {
                return rcre;
            }
            current = current.getCause();
        }
        return null;
    }
}

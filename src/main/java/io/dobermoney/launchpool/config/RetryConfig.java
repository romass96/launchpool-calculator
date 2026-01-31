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

/**
 * Configuration for retry behavior when calling Coingecko API.
 * Handles 429 rate limit responses by retrying after the duration specified in the Retry-After header.
 */
@Slf4j
@Configuration
public class RetryConfig {
    private static final int MAX_ATTEMPTS = 5;
    private static final long DEFAULT_RETRY_AFTER_SECONDS = 60;

    /**
     * Creates a RetryTemplate bean configured for Coingecko API rate limit handling.
     * Retries up to 5 times on 429 responses, waiting for the Retry-After header duration between attempts.
     *
     * @return configured RetryTemplate for Coingecko API calls
     */
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
     * Parses Retry-After header from the exception. Supports delay in seconds (e.g. "60")
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
        String value = headers.getFirst(HttpHeaders.RETRY_AFTER);
        if (value == null || value.isBlank()) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        value = value.trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
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

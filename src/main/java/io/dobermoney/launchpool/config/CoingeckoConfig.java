package io.dobermoney.launchpool.config;

import io.dobermoney.launchpool.client.CoingeckoClient;
import io.dobermoney.launchpool.config.properties.CoingeckoProperties;
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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

/**
 * Configuration for Coingecko API integration.
 * Defines the HTTP client, retry template with Retry-After header support for rate limits.
 */
@Slf4j
@Configuration
public class CoingeckoConfig {
    private static final String API_KEY_HEADER = "x_cg_pro_api_key";
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long DEFAULT_RETRY_AFTER_SECONDS = 60;

    public static final String COINGECKO_RETRY_TEMPLATE_BEAN = "coingeckoRetryTemplate";

    /**
     * Creates the Coingecko HTTP client with API key and base URL configured.
     *
     * @param properties Coingecko API configuration
     * @return configured CoingeckoClient
     */
    @Bean
    public CoingeckoClient coingeckoClient(CoingeckoProperties properties) {
        var restClient = RestClient.builder()
                .baseUrl(properties.apiUrl())
                .defaultHeader(API_KEY_HEADER, properties.apiKey())
                .build();

        var httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return httpServiceProxyFactory.createClient(CoingeckoClient.class);
    }

    /**
     * Creates a RetryTemplate configured for Coingecko API rate limit handling.
     * Retries up to 5 times on 429 responses, waiting for the Retry-After header duration between attempts.
     *
     * @return configured RetryTemplate for Coingecko API calls
     */
    @Bean(COINGECKO_RETRY_TEMPLATE_BEAN)
    public RetryTemplate coingeckoRetryTemplate() {
        var retryPolicy = new ExceptionClassifierRetryPolicy();
        retryPolicy.setExceptionClassifier(throwable -> {
            if (isRateLimitError(throwable)) {
                return new SimpleRetryPolicy(MAX_RETRY_ATTEMPTS);
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
                        retryAfterSeconds, context.getRetryCount(), MAX_RETRY_ATTEMPTS);
            }
        });

        return retryTemplate;
    }

    private static boolean isRateLimitError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof RestClientResponseException restClientResponseException) {
                return restClientResponseException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Parses Retry-After header from the exception. Supports delay in seconds (e.g. "60").
     */
    private static long parseRetryAfter(Throwable throwable) {
        var exception = findRestClientResponseException(throwable);
        try {
            return Optional.ofNullable(exception)
                    .map(RestClientResponseException::getResponseHeaders)
                    .map(httpHeaders -> httpHeaders.getFirst(HttpHeaders.RETRY_AFTER))
                    .filter(value -> !value.isBlank())
                    .map(String::trim)
                    .map(Long::parseLong)
                    .orElse(DEFAULT_RETRY_AFTER_SECONDS);
        } catch (NumberFormatException ignored) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }

    private static RestClientResponseException findRestClientResponseException(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof RestClientResponseException restClientResponseException) {
                return restClientResponseException;
            }
            current = current.getCause();
        }
        return null;
    }
}

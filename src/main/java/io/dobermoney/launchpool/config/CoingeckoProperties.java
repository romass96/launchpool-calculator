package io.dobermoney.launchpool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations.coingecko")
public record CoingeckoProperties(String apiKey, String apiUrl) {
}

package io.dobermoney.launchpool.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations.coingecko")
public record CoingeckoProperties(String apiKey, String apiUrl) {
}

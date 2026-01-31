package io.dobermoney.launchpool.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Coingecko API integration.
 *
 * @param apiKey API key for Coingecko Pro API (sent as x_cg_pro_api_key header)
 * @param apiUrl base URL for Coingecko API (e.g. <a href="https://api.coingecko.com/api/v3">...</a>)
 */
@ConfigurationProperties(prefix = "integrations.coingecko")
public record CoingeckoProperties(String apiKey, String apiUrl) {
}

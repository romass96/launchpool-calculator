package io.dobermoney.launchpool.service.impl;

import io.dobermoney.launchpool.model.Coin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import java.time.Instant;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.junit5.WireMockExtension.newInstance;

/**
 * Integration test for {@link CoingeckoCoinService}.
 * Uses WireMock to simulate the Coingecko API, testing real HTTP client execution
 * and request/response serialization/deserialization.
 */
@SpringBootTest
@ActiveProfiles("test")
class CoingeckoCoinServiceIT {

    private static final String COIN_ID = "bitcoin";
    private static final String COIN_NAME = "Bitcoin";
    private static final String COIN_SYMBOL = "btc";
    private static final String COIN_IMAGE = "https://example.com/btc.png";

    @RegisterExtension
    static WireMockExtension wireMock = newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("integrations.coingecko.api-url",
                () -> "http://localhost:" + wireMock.getPort() + "/api/v3");
    }

    @Autowired
    private CoingeckoCoinService coingeckoCoinService;

    @Test
    void readCoins_returnsCoinsFromAllPages() {
        var coinJson = """
                [{"id":"%s","name":"%s","symbol":"%s","image":"%s","market_cap_rank":1}]
                """.formatted(COIN_ID, COIN_NAME, COIN_SYMBOL, COIN_IMAGE);

        wireMock.stubFor(get(urlPathEqualTo("/api/v3/coins/markets"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(coinJson)));

        var coins = coingeckoCoinService.readCoins();

        assertThat(coins).hasSize(6); // 6 pages, 1 coin per page
        var coin = coins.iterator().next();
        assertThat(coin.getId()).isEqualTo(COIN_ID);
        assertThat(coin.getName()).isEqualTo(COIN_NAME);
        assertThat(coin.getSymbol()).isEqualTo(COIN_SYMBOL);
        assertThat(coin.getImage()).isEqualTo(COIN_IMAGE);
    }

    @Test
    void readPrices_returnsPriceDataForCoin() {
        var coin = Coin.builder()
                .id(COIN_ID)
                .name(COIN_NAME)
                .symbol(COIN_SYMBOL)
                .image(COIN_IMAGE)
                .build();

        var timestamp = 1704067200000L; // 2024-01-01 00:00:00 UTC
        var price = 42000.5;
        var pricesJson = """
                {"prices":[[%d,%s]]}
                """.formatted(timestamp, price);

        wireMock.stubFor(get(urlPathEqualTo("/api/v3/coins/bitcoin/market_chart/range"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(pricesJson)));

        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-02T00:00:00Z");

        var prices = coingeckoCoinService.readPrices(from, to, coin);

        assertThat(prices).hasSize(1);
        var coinPrice = prices.iterator().next();
        assertThat(coinPrice.price()).isEqualTo(price);
        assertThat(coinPrice.timestamp()).isEqualTo(Instant.ofEpochMilli(timestamp));
        assertThat(coinPrice.coin()).isEqualTo(coin);
    }
}

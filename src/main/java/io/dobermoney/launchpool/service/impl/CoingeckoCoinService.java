package io.dobermoney.launchpool.service.impl;

import io.dobermoney.launchpool.client.CoingeckoClient;
import io.dobermoney.launchpool.client.response.CoingeckoCoinResponse;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;
import io.dobermoney.launchpool.model.Currency;
import io.dobermoney.launchpool.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.dobermoney.launchpool.config.CoingeckoConfig.COINGECKO_RETRY_TEMPLATE_BEAN;

/**
 * CoinService implementation that fetches data from the Coingecko API.
 * Uses retry logic with Retry-After header support for rate limit (429) responses.
 */
@Service
@RequiredArgsConstructor
public class CoingeckoCoinService implements CoinService {
    private static final String ORDER = "market_cap_desc";
    private static final int PAGE_SIZE = 250;

    private final CoingeckoClient coingeckoClient;
    @Qualifier(COINGECKO_RETRY_TEMPLATE_BEAN)
    private final RetryTemplate retryTemplate;

    @Override
    public Set<Coin> readCoins() {
        return IntStream.rangeClosed(1, 6) // 6 pages: 6 * 250 = 1500 coins
                .mapToObj(this::readCoinsWithRetry)
                .flatMap(Collection::stream)
                .map(this::toCoin)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<CoinPrice> readPrices(ZonedDateTime from, ZonedDateTime to, Coin coin) {
        return retryTemplate.execute(context -> {
            var response = coingeckoClient.coinsMarketChartRange(
                    coin.getId(),
                    Currency.USD.getCode(),
                    from.toInstant().getEpochSecond(),
                    to.toInstant().getEpochSecond()
            );
            return response.getPrices()
                    .stream()
                    .map(data -> toCoinPrice(data, coin))
                    .collect(Collectors.toSet());
        });
    }

    private List<CoingeckoCoinResponse> readCoinsWithRetry(int page) {
        return retryTemplate.execute(context ->
                coingeckoClient.coinsMarkets(Currency.USD.getCode(), ORDER, PAGE_SIZE, page));
    }

    private Coin toCoin(CoingeckoCoinResponse coinResponse) {
        return Coin.builder()
                .id(coinResponse.getId())
                .name(coinResponse.getName())
                .symbol(coinResponse.getSymbol())
                .image(coinResponse.getImage())
                .build();
    }

    private CoinPrice toCoinPrice(List<Double> data, Coin coin) {
        return new CoinPrice(
                data.get(1),
                Instant.ofEpochMilli(data.get(0).longValue()),
                coin
        );
    }

}

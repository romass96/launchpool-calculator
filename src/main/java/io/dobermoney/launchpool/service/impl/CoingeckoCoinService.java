package io.dobermoney.launchpool.service.impl;

import io.dobermoney.launchpool.client.CoingeckoClient;
import io.dobermoney.launchpool.client.response.CoingeckoCoinResponse;
import io.dobermoney.launchpool.config.CoingeckoProperties;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;
import io.dobermoney.launchpool.model.Currency;
import io.dobermoney.launchpool.service.CoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class CoingeckoCoinService implements CoinService {
    private static final String API_KEY_HEADER = "x_cg_pro_api_key";
    private static final String ORDER = "market_cap_desc";
    private static final int PAGE_SIZE = 250;

    private final CoingeckoClient coingeckoClient;
    private final RetryTemplate retryTemplate;

    public CoingeckoCoinService(@Qualifier("coingeckoRetryTemplate") RetryTemplate retryTemplate,
            CoingeckoProperties coingeckoProperties) {
        this.retryTemplate = retryTemplate;

        var restClient = RestClient.builder()
                .baseUrl(coingeckoProperties.apiUrl())
                .defaultHeader(API_KEY_HEADER, coingeckoProperties.apiKey())
                .build();

        var httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        this.coingeckoClient = httpServiceProxyFactory.createClient(CoingeckoClient.class);
    }

    @Override
    public Set<Coin> readCoins() {
        return IntStream.rangeClosed(1, 6) // 6 pages: 6 * 250 = 1500 coins
                .mapToObj(page -> retryTemplate.execute(context ->
                        coingeckoClient.coinsMarkets(Currency.USD.getCode(), ORDER, PAGE_SIZE, page)))
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

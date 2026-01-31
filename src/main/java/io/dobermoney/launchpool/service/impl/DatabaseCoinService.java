package io.dobermoney.launchpool.service.impl;

import io.dobermoney.launchpool.entity.JpaCoin;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;
import io.dobermoney.launchpool.repository.CoinRepository;
import io.dobermoney.launchpool.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Primary CoinService implementation that serves coins from the database
 * and delegates price lookups to Coingecko. Coins are synced from Coingecko periodically.
 */
@Service
@Primary
@RequiredArgsConstructor
public class DatabaseCoinService implements CoinService {
    private final CoinRepository coinRepository;
    private final CoingeckoCoinService delegate;

    @Override
    public Set<Coin> readCoins() {
        return coinRepository.findAll().stream()
                .map(this::toCoin)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<CoinPrice> readPrices(ZonedDateTime from, ZonedDateTime to, Coin coin) {
        return delegate.readPrices(from, to, coin);
    }

    private Coin toCoin(JpaCoin entity) {
        return Coin.builder()
                .id(entity.getId())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .image(entity.getImage())
                .build();
    }
}

package io.dobermoney.launchpool.scheduler;

import io.dobermoney.launchpool.entity.JpaCoin;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.repository.CoinRepository;
import io.dobermoney.launchpool.service.impl.CoingeckoCoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoinSyncScheduler {
    private final CoingeckoCoinService coingeckoCoinService;
    private final CoinRepository coinRepository;

    @Scheduled(fixedRateString = "PT10M", initialDelayString = "PT0S")
    @Transactional
    public void syncCoins() {
        log.info("Syncing coins from Coingecko...");
        try {
            Set<Coin> coins = coingeckoCoinService.readCoins();
            var entities = coins.stream()
                    .map(this::toEntity)
                    .collect(Collectors.toSet());
            coinRepository.deleteAllInBatch();
            coinRepository.saveAll(entities);
            log.info("Synced {} coins to database", entities.size());
        } catch (Exception e) {
            log.error("Failed to sync coins from Coingecko", e);
        }
    }

    private JpaCoin toEntity(Coin coin) {
        return JpaCoin.builder()
                .id(coin.getId())
                .name(coin.getName())
                .symbol(coin.getSymbol())
                .image(coin.getImage())
                .build();
    }
}

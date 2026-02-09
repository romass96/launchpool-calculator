package io.dobermoney.launchpool.calculator;

import io.dobermoney.launchpool.calculator.request.AverageBalanceCalculationRequest;
import io.dobermoney.launchpool.model.*;
import io.dobermoney.launchpool.model.Currency;
import io.dobermoney.launchpool.service.CoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Calculates the time-weighted average balance in USD for a launchpool period
 * based on transactions and historical coin prices.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AverageBalanceCalculator {
    private final CoinService coinService;

    /**
     * Calculates the average balance in the requested currency for the given period.
     *
     * @param request the calculation request with transactions and time range
     * @return the time-weighted average balance
     * @throws IllegalArgumentException if the period is invalid (from after to, or null)
     */
    public double calculate(AverageBalanceCalculationRequest request) {
        var from = request.getFrom();
        var to = request.getTo();
        if (from == null || to == null || from.isAfter(to)) {
            throw new IllegalArgumentException("Invalid period is provided");
        }

        var coins = request.getTransactions()
                .stream()
                .map(Transaction::getCoin)
                .collect(Collectors.toSet());

        Map<Coin, Set<CoinPrice>> coinPriceMap = coins.stream()
                .collect(Collectors.toMap(Function.identity(), coin -> fetchCoinPrices(coin, from, to)));

        var hourlyBalances = new ArrayList<Map<String, Double>>();
        var previousBalance = new HashMap<String, Double>();

        var rangeStart = from.withMinute(0);
        var rangeEnd = rangeStart.plusHours(1);

        while (rangeStart.isBefore(to)) {
            var hourlyBalance = new HashMap<String, Double>();
            hourlyBalance.put(Currency.USD.getCode(), 0D);
            for (var coin : coins) {
                hourlyBalance.put(coin.getId(), previousBalance.getOrDefault(coin.getId(), 0D));
                final var localRangeStart = rangeStart;
                final var localRangeEnd = rangeEnd;
                request.getTransactions()
                        .stream()
                        .filter(transaction -> transaction.getCoin().equals(coin))
                        .filter(transaction -> isInRange(localRangeStart, localRangeEnd, transaction))
                        .sorted(Comparator.comparing(Transaction::getDateTime))
                        .forEachOrdered(transaction -> {
                            var balance = calculateHourlyBalance(transaction, hourlyBalance.get(coin.getId()));
                            hourlyBalance.put(coin.getId(), balance);
                        });
                var coinPrices = coinPriceMap.get(coin);
                var coinHourlyBalance = hourlyBalance.get(coin.getId());
                var usdHourlyBalanceChange = coinHourlyBalance == 0 ?
                        0 : coinHourlyBalance * findCoinPriceAtRange(rangeStart, rangeEnd, coinPrices);
                hourlyBalance.compute(Currency.USD.getCode(), (key, value) -> value + usdHourlyBalanceChange);
                System.out.println(hourlyBalance.get(Currency.USD.getCode()) + " , start = " + rangeStart + ", end = " + rangeEnd);
            }
            hourlyBalances.add(hourlyBalance);
            previousBalance = hourlyBalance;
            rangeStart = rangeEnd;
            rangeEnd = rangeStart.plusHours(1);
        }

        System.out.println(hourlyBalances);

        var sum = hourlyBalances.stream()
                .mapToDouble(balance -> balance.get(Currency.USD.getCode()))
                .sum();

        return sum / hourlyBalances.size();
    }

    private double calculateHourlyBalance(Transaction transaction, double previousBalance) {
        return transaction.getType() == TransactionType.DEPOSIT ?
                previousBalance + transaction.getAmount() :
                previousBalance - transaction.getAmount();
    }

    private Set<CoinPrice> fetchCoinPrices(Coin coin, ZonedDateTime from, ZonedDateTime to) {
        return coinService.readPrices(from.minusHours(1), to.plusHours(1), coin);
    }

    private boolean isInRange(ZonedDateTime rangeStart, ZonedDateTime rangeEnd, Transaction transaction) {
        return isInRange(rangeStart, rangeEnd, transaction.getDateTime().toEpochSecond());
    }

    private boolean isInRange(ZonedDateTime rangeStart, ZonedDateTime rangeEnd, long timestamp) {
        return timestamp >= rangeStart.toEpochSecond() && timestamp < rangeEnd.toEpochSecond();
    }

    private double findCoinPriceAtRange(ZonedDateTime rangeStart, ZonedDateTime rangeEnd, Set<CoinPrice> coinPrices) {
        log.info("Searching coin price within date range {} - {}", rangeStart, rangeEnd);
        return coinPrices.stream()
                .filter(coinPrice -> isInRange(rangeStart, rangeEnd, coinPrice.timestamp().getEpochSecond()))
                .map(CoinPrice::price)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unable to find coin price for date range " + rangeStart + " - " + rangeEnd));
    }
}

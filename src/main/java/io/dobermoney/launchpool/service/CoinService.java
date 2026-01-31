package io.dobermoney.launchpool.service;

import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Service for retrieving cryptocurrency data including available coins and historical prices.
 */
public interface CoinService {

    /**
     * Returns the set of available coins.
     *
     * @return set of coins available for transactions
     */
    Set<Coin> readCoins();

    /**
     * Returns historical price data for a coin within the specified time range.
     *
     * @param from start of the time range (inclusive)
     * @param to   end of the time range (inclusive)
     * @param coin the coin to fetch prices for
     * @return set of coin prices within the range
     */
    Set<CoinPrice> readPrices(ZonedDateTime from, ZonedDateTime to, Coin coin);

}

package io.dobermoney.launchpool.service;

import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;

import java.time.ZonedDateTime;
import java.util.Set;

public interface CoinService {

    Set<Coin> readCoins();

    Set<CoinPrice> readPrices(ZonedDateTime from, ZonedDateTime to, Coin coin);

}

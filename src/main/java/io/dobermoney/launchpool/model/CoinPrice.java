package io.dobermoney.launchpool.model;

import java.time.Instant;

public record CoinPrice(double price, Instant timestamp, Coin coin) {
}

package io.dobermoney.launchpool.model;

import java.time.Instant;

/**
 * Represents a single price point for a coin at a specific timestamp.
 *
 * @param price    the price value in the target currency
 * @param timestamp when the price was recorded
 * @param coin     the coin this price is for
 */
public record CoinPrice(double price, Instant timestamp, Coin coin) {
}

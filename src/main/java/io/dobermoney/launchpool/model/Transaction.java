package io.dobermoney.launchpool.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * Represents a single launchpool transaction (deposit or withdrawal).
 */
@Setter
@Getter
@Builder
public class Transaction {
    private ZonedDateTime dateTime;
    private TransactionType type;
    private Coin coin;
    private double amount;
}

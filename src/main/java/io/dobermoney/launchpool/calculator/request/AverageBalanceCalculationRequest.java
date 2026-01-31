package io.dobermoney.launchpool.calculator.request;

import io.dobermoney.launchpool.model.Currency;
import io.dobermoney.launchpool.model.Transaction;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Request object for average balance calculation over a launchpool period.
 */
@Builder
@Getter
public class AverageBalanceCalculationRequest {
    private List<Transaction> transactions;
    private ZonedDateTime from;
    private ZonedDateTime to;
    private Currency currency;
}

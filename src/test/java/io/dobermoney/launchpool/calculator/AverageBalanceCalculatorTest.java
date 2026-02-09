package io.dobermoney.launchpool.calculator;

import io.dobermoney.launchpool.calculator.request.AverageBalanceCalculationRequest;
import io.dobermoney.launchpool.model.Coin;
import io.dobermoney.launchpool.model.CoinPrice;
import io.dobermoney.launchpool.model.Transaction;
import io.dobermoney.launchpool.model.TransactionType;
import io.dobermoney.launchpool.service.CoinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AverageBalanceCalculator}.
 */
@ExtendWith(MockitoExtension.class)
class AverageBalanceCalculatorTest {

    private static final Coin BTC = Coin.builder()
            .id("bitcoin")
            .name("Bitcoin")
            .symbol("btc")
            .image("https://example.com/btc.png")
            .build();

    @Mock
    private CoinService coinService;

    private AverageBalanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AverageBalanceCalculator(coinService);
    }

    @Test
    void calculate_throwsWhenFromIsNull() {
        var request = AverageBalanceCalculationRequest.builder()
                .from(null)
                .to(ZonedDateTime.parse("2024-01-02T00:00:00Z"))
                .transactions(List.of())
                .build();

        assertThatThrownBy(() -> calculator.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid period is provided");
    }

    @Test
    void calculate_throwsWhenToIsNull() {
        var request = AverageBalanceCalculationRequest.builder()
                .from(ZonedDateTime.parse("2024-01-01T00:00:00Z"))
                .to(null)
                .transactions(List.of())
                .build();

        assertThatThrownBy(() -> calculator.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid period is provided");
    }

    @Test
    void calculate_throwsWhenFromIsAfterTo() {
        var request = AverageBalanceCalculationRequest.builder()
                .from(ZonedDateTime.parse("2024-01-02T00:00:00Z"))
                .to(ZonedDateTime.parse("2024-01-01T00:00:00Z"))
                .transactions(List.of())
                .build();

        assertThatThrownBy(() -> calculator.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid period is provided");
    }

    @Test
    void calculate_throwsWhenNoPriceForRange() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T02:00:00Z");
        var deposit = Transaction.builder()
                .dateTime(from.plusMinutes(30))
                .type(TransactionType.DEPOSIT)
                .coin(BTC)
                .amount(1.0)
                .build();

        when(coinService.readPrices(any(), any(), eq(BTC)))
                .thenReturn(Set.of()); // no prices

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of(deposit))
                .build();

        assertThatThrownBy(() -> calculator.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unable to find coin price for date range");
    }

    @Test
    void calculate_returnsZeroForEmptyTransactions() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T02:00:00Z");

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of())
                .build();

        var result = calculator.calculate(request);

        assertThat(result).isZero();
    }

    @Test
    void calculate_singleDeposit_singleHour() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T01:00:00Z");
        var priceInstant = Instant.parse("2024-01-01T00:30:00Z");
        var price = new CoinPrice(10000.0, priceInstant, BTC);

        var deposit = Transaction.builder()
                .dateTime(from.plusMinutes(30))
                .type(TransactionType.DEPOSIT)
                .coin(BTC)
                .amount(1.0)
                .build();

        when(coinService.readPrices(any(), any(), eq(BTC)))
                .thenReturn(Set.of(price));

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of(deposit))
                .build();

        var result = calculator.calculate(request);

        assertThat(result).isEqualTo(10000.0); // 1 BTC * 10000
    }

    @Test
    void calculate_depositAndWithdraw_sameHour() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T01:00:00Z");
        var priceInstant = Instant.parse("2024-01-01T00:30:00Z");
        var price = new CoinPrice(10000.0, priceInstant, BTC);

        var deposit = Transaction.builder()
                .dateTime(from.plusMinutes(10))
                .type(TransactionType.DEPOSIT)
                .coin(BTC)
                .amount(2.0)
                .build();
        var withdraw = Transaction.builder()
                .dateTime(from.plusMinutes(30))
                .type(TransactionType.WITHDRAW)
                .coin(BTC)
                .amount(0.5)
                .build();

        when(coinService.readPrices(any(), any(), eq(BTC)))
                .thenReturn(Set.of(price));

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of(deposit, withdraw))
                .build();

        var result = calculator.calculate(request);

        assertThat(result).isEqualTo(15000.0); // (2 - 0.5) BTC * 10000
    }

    @Test
    void calculate_twoHours_twoPrices() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T02:00:00Z");
        var price1 = new CoinPrice(10000.0, Instant.parse("2024-01-01T00:30:00Z"), BTC);
        var price2 = new CoinPrice(20000.0, Instant.parse("2024-01-01T01:30:00Z"), BTC);

        var deposit = Transaction.builder()
                .dateTime(from.plusMinutes(30))
                .type(TransactionType.DEPOSIT)
                .coin(BTC)
                .amount(1.0)
                .build();

        when(coinService.readPrices(any(), any(), eq(BTC)))
                .thenReturn(Set.of(price1, price2));

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of(deposit))
                .build();

        var result = calculator.calculate(request);

        // Hour 1: 1 BTC * 10000 = 10000
        // Hour 2: 1 BTC * 20000 = 20000
        // Average: (10000 + 20000) / 2 = 15000
        assertThat(result).isEqualTo(15000.0);
    }

    @Test
    void calculate_transactionInSecondHour() {
        var from = ZonedDateTime.parse("2024-01-01T00:00:00Z");
        var to = ZonedDateTime.parse("2024-01-01T03:00:00Z");
        var price1 = new CoinPrice(10000.0, Instant.parse("2024-01-01T00:30:00Z"), BTC);
        var price2 = new CoinPrice(20000.0, Instant.parse("2024-01-01T01:30:00Z"), BTC);
        var price3 = new CoinPrice(30000.0, Instant.parse("2024-01-01T02:30:00Z"), BTC);

        var deposit = Transaction.builder()
                .dateTime(from.plusHours(1).plusMinutes(30))
                .type(TransactionType.DEPOSIT)
                .coin(BTC)
                .amount(1.0)
                .build();

        when(coinService.readPrices(any(), any(), eq(BTC)))
                .thenReturn(Set.of(price1, price2, price3));

        var request = AverageBalanceCalculationRequest.builder()
                .from(from)
                .to(to)
                .transactions(List.of(deposit))
                .build();

        var result = calculator.calculate(request);

        // Hour 1: 0 BTC * 10000 = 0
        // Hour 2: 1 BTC * 20000 = 20000
        // Hour 3: 1 BTC * 30000 = 30000
        // Average: (0 + 20000 + 30000) / 3 = 16666.666...
        assertThat(result).isEqualTo(50000.0 / 3);
    }
}

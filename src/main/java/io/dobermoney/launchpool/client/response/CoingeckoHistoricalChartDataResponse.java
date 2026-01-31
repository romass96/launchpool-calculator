package io.dobermoney.launchpool.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for Coingecko market_chart/range API response.
 * Prices are lists of [timestamp_ms, price] pairs.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoingeckoHistoricalChartDataResponse {
    private List<List<Double>> prices;
}

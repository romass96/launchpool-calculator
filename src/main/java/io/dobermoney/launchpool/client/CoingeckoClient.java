package io.dobermoney.launchpool.client;


import io.dobermoney.launchpool.client.response.CoingeckoCoinResponse;
import io.dobermoney.launchpool.client.response.CoingeckoHistoricalChartDataResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/**
 * HTTP client interface for the Coingecko API.
 * Base URL is configured via RestClient when the client is created.
 */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface CoingeckoClient {

    /**
     * Fetches a page of coins from the markets endpoint.
     *
     * @param currency target currency (e.g. usd)
     * @param order    sort order (e.g. market_cap_desc)
     * @param pageSize number of coins per page
     * @param page     page number (1-based)
     * @return list of coins for the requested page
     */
    @GetExchange("/coins/markets")
    List<CoingeckoCoinResponse> coinsMarkets(
            @RequestParam("vs_currency") String currency,
            @RequestParam("order") String order,
            @RequestParam("per_page") int pageSize,
            @RequestParam("page") int page
    );

    /**
     * Fetches historical market chart data for a coin within a time range.
     *
     * @param id       coin id (e.g. bitcoin)
     * @param currency target currency (e.g. usd)
     * @param from     start timestamp in seconds since epoch
     * @param to       end timestamp in seconds since epoch
     * @return response containing price data points
     */
    @GetExchange("/coins/{id}/market_chart/range")
    CoingeckoHistoricalChartDataResponse coinsMarketChartRange(
            @PathVariable("id") String id,
            @RequestParam("vs_currency") String currency,
            @RequestParam("from") long from,
            @RequestParam("to") long to
    );

}
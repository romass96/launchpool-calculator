package io.dobermoney.launchpool.client;


import io.dobermoney.launchpool.client.response.CoingeckoCoinResponse;
import io.dobermoney.launchpool.client.response.CoingeckoHistoricalChartDataResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE)
public interface CoingeckoClient {

    @GetExchange("/coins/markets")
    List<CoingeckoCoinResponse> coinsMarkets(
            @RequestParam("vs_currency") String currency,
            @RequestParam("order") String order,
            @RequestParam("per_page") int pageSize,
            @RequestParam("page") int page
    );

    @GetExchange("/coins/{id}/market_chart/range")
    CoingeckoHistoricalChartDataResponse coinsMarketChartRange(
            @PathVariable("id") String id,
            @RequestParam("vs_currency") String currency,
            @RequestParam("from") long from,
            @RequestParam("to") long to
    );

}
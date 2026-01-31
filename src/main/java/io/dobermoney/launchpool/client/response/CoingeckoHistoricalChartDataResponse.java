package io.dobermoney.launchpool.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoingeckoHistoricalChartDataResponse {
    private List<List<Double>> prices;
}

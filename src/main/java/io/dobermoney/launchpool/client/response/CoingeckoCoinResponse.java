package io.dobermoney.launchpool.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Coingecko /coins/markets API response.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoingeckoCoinResponse {
    private String id;
    private String name;
    private String symbol;
    private String image;

    @JsonProperty("market_cap_rank")
    private int marketCapRank;
}

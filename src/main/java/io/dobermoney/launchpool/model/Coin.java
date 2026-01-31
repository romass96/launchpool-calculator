package io.dobermoney.launchpool.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
public class Coin {
    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private String symbol;
    private String image;

    @Override
    public String toString() {
        return symbol.toUpperCase();
    }
}

package io.dobermoney.launchpool.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Supported fiat currencies for price display.
 */
@Getter
@RequiredArgsConstructor
public enum Currency {
    USD("usd");

    private final String code;
}

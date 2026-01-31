package io.dobermoney.launchpool.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {
    USD("usd");

    private final String code;
}

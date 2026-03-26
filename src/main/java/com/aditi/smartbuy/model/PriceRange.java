package com.aditi.smartbuy.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@RequiredArgsConstructor
public class PriceRange {
    private final BigDecimal maxPrice;
    private final BigDecimal minPrice;
}

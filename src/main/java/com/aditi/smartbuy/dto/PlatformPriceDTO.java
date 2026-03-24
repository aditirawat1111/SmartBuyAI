package com.aditi.smartbuy.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformPriceDTO {
    private String platformName;
    private BigDecimal currentPrice;
    private BigDecimal rating;
    private Integer deliveryDays;
    private String productUrl;
    private Double smartScore;
}

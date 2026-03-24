package com.aditi.smartbuy.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareProductResponse {
    private ProductResponse product;
    private List<PlatformPriceDTO> platformPrices;
    private PlatformPriceDTO bestDeal;
}

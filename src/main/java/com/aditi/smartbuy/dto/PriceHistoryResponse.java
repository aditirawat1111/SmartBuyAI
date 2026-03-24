package com.aditi.smartbuy.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryResponse {
    private ProductResponse product;
    private List<PriceHistoryDTO> priceHistory;
}

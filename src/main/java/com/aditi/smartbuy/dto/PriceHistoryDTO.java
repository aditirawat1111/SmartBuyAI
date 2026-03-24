package com.aditi.smartbuy.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryDTO {
    private Long id;
    private String platformName;
    private BigDecimal price;
    private LocalDateTime recordedAt;
}

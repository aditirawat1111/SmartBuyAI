package com.aditi.smartbuy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_platform_mapping_id", nullable = false)
    private ProductPlatformMapping productPlatformMapping;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}

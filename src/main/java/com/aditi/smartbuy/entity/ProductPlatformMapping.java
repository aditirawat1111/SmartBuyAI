package com.aditi.smartbuy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_platform_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "platform_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPlatformMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    /**
     * MSRP anchor. Simulation varies price relative to this (avoids random-walk drift on current price).
     */
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @Column(name = "product_url")
    private String productUrl;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @OneToMany(mappedBy = "productPlatformMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PriceHistory> priceHistories = new ArrayList<>();

    @PrePersist
    protected void ensureBasePriceOnPersist() {
        if (basePrice == null && currentPrice != null) {
            basePrice = currentPrice;
        }
    }

    @PreUpdate
    protected void touchLastUpdated() {
        lastUpdated = LocalDateTime.now();
    }
}

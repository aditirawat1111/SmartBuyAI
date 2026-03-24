package com.aditi.smartbuy.service;

import com.aditi.smartbuy.entity.ProductPlatformMapping;
import com.aditi.smartbuy.model.PriceRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Smart Score Logic: Weighted formula combining price, rating, and delivery.
 * - Lower price → higher score
 * - Higher rating → higher score
 * - Fewer delivery days → higher score
 */
@Service
@Slf4j
public class SmartScoreService {

    private static final double PRICE_WEIGHT = 0.50;  // 50%
    private static final double RATING_WEIGHT = 0.30; // 30%
    private static final double DELIVERY_WEIGHT = 0.20; // 20%

    private static final int MAX_DELIVERY_DAYS = 14;
    private static final int MAX_RATING = 5;

    /**
     * Calculate smart score for a single ProductPlatformMapping.
     * Returns a score from 0-100 (higher is better).
     */
    public double calculateSmartScore(ProductPlatformMapping mapping,
                                      PriceRange priceRange) {
        double priceScore = calculatePriceScore(mapping.getCurrentPrice(), priceRange);
        double ratingScore = calculateRatingScore(mapping.getRating());
        double deliveryScore = calculateDeliveryScore(mapping.getDeliveryDays());

        double score = (PRICE_WEIGHT * priceScore)
                + (RATING_WEIGHT * ratingScore)
                + (DELIVERY_WEIGHT * deliveryScore);

        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Lower price = higher score. Best price gets 100, worst gets 0.
     */
    private double calculatePriceScore(BigDecimal price, PriceRange priceRange) {
        BigDecimal maxPrice = priceRange.getMaxPrice();
        BigDecimal minPrice = priceRange.getMinPrice();

        if (minPrice.equals(maxPrice)) return 100.0;

        // Invert: lower price = higher score
        double range = maxPrice.subtract(minPrice).doubleValue();
        double distanceFromMin = price.subtract(minPrice).doubleValue();
        double normalized = 1 - (distanceFromMin / range);
        return Math.max(0, Math.min(100, normalized * 100));
    }

    /**
     * Higher rating = higher score. 5 stars = 100.
     */
    private double calculateRatingScore(BigDecimal rating) {
        if (rating == null) return 50.0; // neutral
        double r = rating.doubleValue();
        return (r / MAX_RATING) * 100;
    }

    /**
     * Fewer delivery days = higher score. Same day = 100.
     */
    private double calculateDeliveryScore(Integer deliveryDays) {
        if (deliveryDays == null) return 50.0; // neutral
        if (deliveryDays <= 0) return 100.0;
        double d = Math.min(deliveryDays, MAX_DELIVERY_DAYS);
        double normalized = 1 - (d / MAX_DELIVERY_DAYS);
        return Math.max(0, (normalized + (1.0 / MAX_DELIVERY_DAYS)) * 100);
    }

}

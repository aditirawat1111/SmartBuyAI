package com.aditi.smartbuy.service;

import com.aditi.smartbuy.dto.*;
import com.aditi.smartbuy.entity.Product;
import com.aditi.smartbuy.entity.ProductPlatformMapping;
import com.aditi.smartbuy.model.PriceRange;
import com.aditi.smartbuy.repository.ProductPlatformMappingRepository;
import com.aditi.smartbuy.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompareService {

    private final ProductRepository productRepository;
    private final ProductPlatformMappingRepository mappingRepository;
    private final SmartScoreService smartScoreService;

    /**
     * Compare product prices across all platforms.
     * Returns product info, price per platform with smart score, and best deal.
     */
    public CompareProductResponse compareProduct(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;

        // Get all the mappings(product and platform details) for the product
        List<ProductPlatformMapping> mappings = mappingRepository.findByProductIdWithDetails(productId);
        if (mappings.isEmpty()) {
            return CompareProductResponse.builder()
                    .product(ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .category(product.getCategory())
                            .imageUrl(product.getImageUrl())
                            .build())
                    .platformPrices(List.of())
                    .bestDeal(null)
                    .build();
        }

        // Get the price range for the product
        PriceRange priceRange=getPriceRange(mappings);

        // Get the necessary details of the product on each platform in sorted order of smart score
        List<PlatformPriceDTO> platformPrices = mappings.stream()
                .map(m -> PlatformPriceDTO.builder()
                        .platformName(m.getPlatform().getName())
                        .currentPrice(m.getCurrentPrice())
                        .rating(m.getRating())
                        .deliveryDays(m.getDeliveryDays())
                        .productUrl(m.getProductUrl())
                        .smartScore(smartScoreService.calculateSmartScore(m, priceRange))
                        .build())
                .sorted(Comparator.comparingDouble(PlatformPriceDTO::getSmartScore)
                .reversed())
                .collect(Collectors.toList());

        // Get the best deal(platform) for the product using the smart score service
        PlatformPriceDTO bestDeal=platformPrices.stream()
                .max(Comparator.comparingDouble(PlatformPriceDTO::getSmartScore))
                .orElse(null);

        // build and return the responseDTOs
        return CompareProductResponse.builder()
                .product(ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .category(product.getCategory())
                        .imageUrl(product.getImageUrl())
                        .build())
                .platformPrices(platformPrices)
                .bestDeal(bestDeal)
                .build();
    }

    private PriceRange getPriceRange(List<ProductPlatformMapping> mappings){
        BigDecimal maxPrice=mappings.stream()
                .map(ProductPlatformMapping::getCurrentPrice)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal minPrice=mappings.stream()
                .map(ProductPlatformMapping::getCurrentPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return new PriceRange(minPrice, maxPrice);
    }
}
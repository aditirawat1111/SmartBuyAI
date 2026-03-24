package com.aditi.smartbuy.service;

import com.aditi.smartbuy.dto.*;
import com.aditi.smartbuy.entity.Product;
import com.aditi.smartbuy.entity.PriceHistory;
import com.aditi.smartbuy.repository.PriceHistoryRepository;
import com.aditi.smartbuy.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PriceHistoryService {

    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private static final int DEFAULT_HISTORY_LIMIT = 90;

    /**
     * Get price history for a product across all platforms.
     */
    public PriceHistoryResponse getPriceHistory(Long productId, Integer limit) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;

        int maxRecords = limit != null && limit > 0 ? Math.min(limit, 365) : DEFAULT_HISTORY_LIMIT;
        var pageable = PageRequest.of(0, maxRecords);

        List<PriceHistory> history = priceHistoryRepository.findByProductId(productId, pageable);

        List<PriceHistoryDTO> dtos = history.stream()
                .map(this::toPriceHistoryDTO)
                .collect(Collectors.toList());
        
        return PriceHistoryResponse.builder()
                .product(ProductResponse.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .category(product.getCategory())
                                .imageUrl(product.getImageUrl())
                                .build())
                .priceHistory(dtos)
                .build();
                             
        }

    private PriceHistoryDTO toPriceHistoryDTO(PriceHistory priceHistory) {
        return PriceHistoryDTO.builder()
                .id(priceHistory.getId())
                .platformName(priceHistory.getProductPlatformMapping().getPlatform().getName())
                .price(priceHistory.getPrice())
                .recordedAt(priceHistory.getRecordedAt())
                .build();
    }
}
package com.aditi.smartbuy.service;

import com.aditi.smartbuy.dto.ProductResponse;
import com.aditi.smartbuy.entity.Product;
import com.aditi.smartbuy.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll().stream()
                    .map(this::toProductResponse)
                    .collect(Collectors.toList());
        }
        return productRepository.searchByNameOrCategory(query.trim()).stream()
                .map(this::toProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::toProductResponse)
                .orElse(null);
    }

    public Product getProductEntity(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    private ProductResponse toProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .build();
    }
}

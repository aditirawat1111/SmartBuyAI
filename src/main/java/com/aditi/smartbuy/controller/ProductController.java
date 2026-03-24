package com.aditi.smartbuy.controller;

import com.aditi.smartbuy.dto.CompareProductResponse;
import com.aditi.smartbuy.dto.PriceHistoryResponse;
import com.aditi.smartbuy.dto.ProductResponse;
import com.aditi.smartbuy.service.CompareService;
import com.aditi.smartbuy.service.PriceHistoryService;
import com.aditi.smartbuy.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final CompareService compareService;
    private final PriceHistoryService priceHistoryService;

    /**
     * Product Search API - Search products by name or category.
     * GET /api/products/search?q=laptop
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam(required = false) String q) {
        List<ProductResponse> products = productService.searchProducts(q);
        return ResponseEntity.ok(products);
    }

    /**
     * Compare Product API - Returns price per platform, rating, delivery, smart score.
     * GET /api/products/{id}/compare
     */
    @GetMapping("/{id}/compare")
    public ResponseEntity<CompareProductResponse> compareProduct(@PathVariable Long id) {
        CompareProductResponse response = compareService.compareProduct(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Price History API - Returns price trend data.
     * GET /api/products/{id}/price-history?limit=30
     */
    @GetMapping("/{id}/price-history")
    public ResponseEntity<PriceHistoryResponse> getPriceHistory(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "90") Integer limit) {
        PriceHistoryResponse response = priceHistoryService.getPriceHistory(id, limit);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
}

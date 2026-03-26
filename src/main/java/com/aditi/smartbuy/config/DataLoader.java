package com.aditi.smartbuy.config;

import com.aditi.smartbuy.entity.*;
import com.aditi.smartbuy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds initial data: Platforms (Amazon, Flipkart, Myntra) and sample products.
 * Runs only when profile "dev" or default profile is active.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("default")
public class DataLoader implements CommandLineRunner {

    private final PlatformRepository platformRepository;
    private final ProductRepository productRepository;
    private final ProductPlatformMappingRepository mappingRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (platformRepository.count() > 0) {
            log.info("Data already exists. Skipping seed.");
            return;
        }

        log.info("Seeding initial data...");

        // Create platforms
        Platform amazon = Platform.builder().name("Amazon").baseUrl("https://amazon.in").build();
        Platform flipkart = Platform.builder().name("Flipkart").baseUrl("https://flipkart.com").build();
        Platform myntra = Platform.builder().name("Myntra").baseUrl("https://myntra.com").build();

        amazon = platformRepository.save(amazon);
        flipkart = platformRepository.save(flipkart);
        myntra = platformRepository.save(myntra);

        // Create sample products
        Product laptop = Product.builder()
                .name("HP Laptop 15s")
                .category("Electronics")
                .imageUrl("https://example.com/laptop.jpg")
                .build();
        laptop = productRepository.save(laptop);

        Product phone = Product.builder()
                .name("Samsung Galaxy M34 5G")
                .category("Electronics")
                .imageUrl("https://example.com/phone.jpg")
                .build();
        phone = productRepository.save(phone);

        Product headphones = Product.builder()
                .name("boAt Rockerz 450")
                .category("Electronics")
                .imageUrl("https://example.com/headphones.jpg")
                .build();
        headphones = productRepository.save(headphones);

        // Create ProductPlatformMappings with prices
        createMapping(laptop, amazon, new BigDecimal("42999"), new BigDecimal("4.2"), 3);
        createMapping(laptop, flipkart, new BigDecimal("41990"), new BigDecimal("4.5"), 2);
        createMapping(laptop, myntra, new BigDecimal("44999"), new BigDecimal("4.0"), 5);

        createMapping(phone, amazon, new BigDecimal("18999"), new BigDecimal("4.3"), 2);
        createMapping(phone, flipkart, new BigDecimal("17999"), new BigDecimal("4.4"), 3);
        createMapping(phone, myntra, new BigDecimal("19999"), new BigDecimal("4.1"), 4);

        createMapping(headphones, amazon, new BigDecimal("1299"), new BigDecimal("4.5"), 2);
        createMapping(headphones, flipkart, new BigDecimal("1199"), new BigDecimal("4.6"), 1);
        createMapping(headphones, myntra, new BigDecimal("1399"), new BigDecimal("4.4"), 3);

        log.info("Seed data loaded successfully: 3 platforms, 3 products, 9 mappings.");
    }

    private void createMapping(Product product, Platform platform, BigDecimal price,
                              BigDecimal rating, int deliveryDays) {
        ProductPlatformMapping mapping = ProductPlatformMapping.builder()
                .product(product)
                .platform(platform)
                .basePrice(price)
                .currentPrice(price)
                .rating(rating)
                .deliveryDays(deliveryDays)
                .productUrl(platform.getBaseUrl() + "/product/" + product.getId())
                .lastUpdated(LocalDateTime.now())
                .build();
        mapping = mappingRepository.save(mapping);

        // Add initial price history entry
        PriceHistory history = PriceHistory.builder()
                .productPlatformMapping(mapping)
                .price(price)
                .recordedAt(LocalDateTime.now())
                .build();
        priceHistoryRepository.save(history);
    }
}

package com.aditi.smartbuy.scheduler;

import com.aditi.smartbuy.entity.ProductPlatformMapping;
import com.aditi.smartbuy.repository.ProductPlatformMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Paginated orchestration. Each page is processed in PriceSimulationPageProcessor (own transaction).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceSimulationScheduler {

    private final ProductPlatformMappingRepository mappingRepository;
    private final PriceSimulationPageProcessor pageProcessor;

    @Value("${smartbuy.scheduler.page-size:100}")
    private int pageSize;

    @Scheduled(cron = "${smartbuy.scheduler.cron:0 1 0 * * ?}")
    public void simulateDailyPriceChange() {
        log.info("Starting daily price simulation (paginated, base-anchored)...");
        int totalProcessed = 0;
        int pageNumber = 0;

        while (true) {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<ProductPlatformMapping> page = mappingRepository.findAllForSimulation(pageable);
            if (page.isEmpty()) {
                break;
            }

            try {
                pageProcessor.processPage(page.getContent());
                totalProcessed += page.getNumberOfElements();
            } catch (Exception e) {
                log.error("Page {} failed (after retries if transient): {}", pageNumber, e.getMessage(), e);
            }

            if (!page.hasNext()) {
                break;
            }
            pageNumber++;
        }

        log.info("Daily price simulation completed. Successfully updated {} mappings.", totalProcessed);
    }
}

package com.aditi.smartbuy.scheduler;

import com.aditi.smartbuy.entity.PriceHistory;
import com.aditi.smartbuy.entity.ProductPlatformMapping;
import com.aditi.smartbuy.repository.PriceHistoryRepository;
import com.aditi.smartbuy.repository.ProductPlatformMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * One transactional unit of work per page. Invoked from PriceSimulationScheduler
 * so Spring AOP applies transactional and retry proxies (avoids self-invocation).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceSimulationPageProcessor {

    private final ProductPlatformMappingRepository mappingRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    @Value("${smartbuy.scheduler.min-price-factor:0.50}")
    private BigDecimal minPriceFactor;

    @Value("${smartbuy.scheduler.max-price-factor:1.50}")
    private BigDecimal maxPriceFactor;

    @Value("${smartbuy.scheduler.simulation.weekend-adjustment:-0.045}")
    private BigDecimal weekendAdjustment;

    @Value("${smartbuy.scheduler.simulation.demand-max-scale:0.035}")
    private BigDecimal demandMaxScale;

    @Value("${smartbuy.scheduler.simulation.competitor-higher-than-min:-0.05}")
    private BigDecimal competitorHigherThanMin;

    @Value("${smartbuy.scheduler.simulation.competitor-lower-than-min:0.02}")
    private BigDecimal competitorLowerThanMin;

    @Value("${smartbuy.scheduler.simulation.noise-half-range:0.01}")
    private BigDecimal noiseHalfRange;

    @Value("${smartbuy.scheduler.simulation.default-rating:2.5}")
    private BigDecimal defaultRating;

    @Value("${smartbuy.scheduler.simulation.rating-scale-divisor:5}")
    private BigDecimal ratingScaleDivisor;

    @Retryable(
            retryFor = {TransientDataAccessException.class},
            maxAttemptsExpression = "${smartbuy.scheduler.retry.max-attempts:3}",
            backoff = @Backoff(delayExpression = "${smartbuy.scheduler.retry.backoff-ms:500}")
    )
    @Transactional
    public void processPage(List<ProductPlatformMapping> mappings) {
        if (mappings.isEmpty()) {
            return;
        }

        final LocalDateTime batchRecordedAt = LocalDateTime.now();

        Set<Long> productIds = mappings.stream()
                .map(m -> m.getProduct().getId())
                .collect(Collectors.toSet());

        Map<Long, BigDecimal> minPriceByProduct = loadMinPriceByProduct(productIds);
        LocalDate today = batchRecordedAt.toLocalDate();

        List<SimulationInput> inputs = mappings.stream()
                .map(m -> toSimulationInput(m, minPriceByProduct))
                .toList();

        Map<Long, BigDecimal> newPriceById = inputs.parallelStream()
                .map(in -> new SimulationOutput(in.mappingId(), computeAnchoredPrice(in, today)))
                .collect(Collectors.toMap(
                        SimulationOutput::mappingId,
                        SimulationOutput::newPrice,
                        (existing, replacement) -> replacement
                ));

        List<PriceHistory> historyBatch = new ArrayList<>(mappings.size());

        for (ProductPlatformMapping m : mappings) {
            BigDecimal newPrice = newPriceById.get(m.getId());
            if (m.getBasePrice() == null) {
                m.setBasePrice(m.getCurrentPrice());
            }
            m.setCurrentPrice(newPrice);
            m.setLastUpdated(batchRecordedAt);

            historyBatch.add(PriceHistory.builder()
                    .productPlatformMapping(m)
                    .price(newPrice)
                    .recordedAt(batchRecordedAt)
                    .build());
        }

        mappingRepository.saveAll(mappings);
        priceHistoryRepository.saveAll(historyBatch);
    }

    private Map<Long, BigDecimal> loadMinPriceByProduct(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = mappingRepository.findMinCurrentPriceByProductIds(productIds);
        Map<Long, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            Long pid = (Long) row[0];
            BigDecimal minP = (BigDecimal) row[1];
            map.put(pid, minP);
        }
        return map;
    }

    private SimulationInput toSimulationInput(ProductPlatformMapping m, Map<Long, BigDecimal> minByProduct) {
        Long productId = m.getProduct().getId();
        BigDecimal base = m.getBasePrice() != null ? m.getBasePrice() : m.getCurrentPrice();
        BigDecimal minAcross = minByProduct.getOrDefault(productId, m.getCurrentPrice());
        return new SimulationInput(
                m.getId(),
                base,
                m.getCurrentPrice(),
                productId,
                m.getRating(),
                minAcross
        );
    }

    private BigDecimal computeAnchoredPrice(SimulationInput in, LocalDate today) {
        BigDecimal base = in.basePrice();
        BigDecimal minBound = base.multiply(minPriceFactor).setScale(2, RoundingMode.HALF_UP);
        BigDecimal maxBound = base.multiply(maxPriceFactor).setScale(2, RoundingMode.HALF_UP);

        DayOfWeek dow = today.getDayOfWeek();
        boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        BigDecimal weekendAdj = weekend ? weekendAdjustment : BigDecimal.ZERO;

        BigDecimal rating = in.rating() != null ? in.rating() : defaultRating;
        BigDecimal demandAdj = rating
                .divide(ratingScaleDivisor, 8, RoundingMode.HALF_UP)
                .multiply(demandMaxScale);

        BigDecimal competitorAdj = BigDecimal.ZERO;
        int cmp = in.currentPrice().compareTo(in.minPriceAcrossPlatforms());
        if (cmp > 0) {
            competitorAdj = competitorHigherThanMin;
        } else if (cmp < 0) {
            competitorAdj = competitorLowerThanMin;
        }

        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        double half = noiseHalfRange.doubleValue();
        BigDecimal noise = BigDecimal.valueOf((tlr.nextDouble() - 0.5) * 2 * half);

        BigDecimal sumAdj = weekendAdj.add(demandAdj).add(competitorAdj).add(noise);
        BigDecimal factor = BigDecimal.ONE.add(sumAdj);

        BigDecimal raw = base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
        return raw.max(minBound).min(maxBound);
    }

    private record SimulationInput(
            Long mappingId,
            BigDecimal basePrice,
            BigDecimal currentPrice,
            Long productId,
            BigDecimal rating,
            BigDecimal minPriceAcrossPlatforms
    ) {}

    private record SimulationOutput(Long mappingId, BigDecimal newPrice) {}
}

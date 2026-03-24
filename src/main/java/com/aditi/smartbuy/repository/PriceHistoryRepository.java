package com.aditi.smartbuy.repository;

import com.aditi.smartbuy.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductPlatformMappingIdOrderByRecordedAtDesc(
            Long productPlatformMappingId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT ph FROM PriceHistory ph " +
           "JOIN ph.productPlatformMapping ppm " +
           "WHERE ppm.product.id = :productId " +
           "ORDER BY ph.recordedAt DESC")
    List<PriceHistory> findByProductId(@Param("productId") Long productId,
                                       org.springframework.data.domain.Pageable pageable);

    @Query("SELECT ph FROM PriceHistory ph " +
           "JOIN ph.productPlatformMapping ppm " +
           "WHERE ppm.product.id = :productId " +
           "AND ph.recordedAt >= :since " +
           "ORDER BY ph.recordedAt ASC")
    List<PriceHistory> findByProductIdSince(@Param("productId") Long productId,
                                            @Param("since") LocalDateTime since);
}

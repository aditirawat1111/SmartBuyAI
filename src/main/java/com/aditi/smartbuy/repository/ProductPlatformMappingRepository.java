package com.aditi.smartbuy.repository;

import com.aditi.smartbuy.entity.ProductPlatformMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPlatformMappingRepository extends JpaRepository<ProductPlatformMapping, Long> {

    List<ProductPlatformMapping> findByProductId(Long productId);

    Optional<ProductPlatformMapping> findByProductIdAndPlatformId(Long productId, Long platformId);

    @Query("SELECT ppm FROM ProductPlatformMapping ppm " +
           "JOIN FETCH ppm.product p " +
           "JOIN FETCH ppm.platform pl " +
           "WHERE p.id = :productId")
    List<ProductPlatformMapping> findByProductIdWithDetails(@Param("productId") Long productId);

    boolean existsByProductIdAndPlatformId(Long productId, Long platformId);

    @EntityGraph(attributePaths = {"product", "platform"})
    @Query("SELECT m FROM ProductPlatformMapping m")
    Page<ProductPlatformMapping> findAllForSimulation(Pageable pageable);

    @Query("SELECT m.product.id, MIN(m.currentPrice) FROM ProductPlatformMapping m " +
           "WHERE m.product.id IN :productIds GROUP BY m.product.id")
    List<Object[]> findMinCurrentPriceByProductIds(@Param("productIds") Collection<Long> productIds);
}
